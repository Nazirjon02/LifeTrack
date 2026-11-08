package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import kotlinx.io.IOException
import tj.mahram.lifetrack.data.remote.firebase.FS_META_DELETED
import tj.mahram.lifetrack.data.remote.firebase.FS_META_UPDATED_AT
import tj.mahram.lifetrack.data.remote.firebase.FirebaseConfig
import tj.mahram.lifetrack.data.remote.firebase.FsValue
import tj.mahram.lifetrack.data.remote.firebase.RemoteStore
import tj.mahram.lifetrack.domain.repository.AuthRepository

/** UI-facing snapshot of sync progress. */
data class SyncStatus(
    val running: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastError: String? = null
)

/**
 * Offline-first, per-record cloud sync.
 *
 * Every cycle: (1) make sure the local bookkeeping belongs to the signed-in
 * account (a different account resets it — see [SyncTracker.claimFor]),
 * (2) seed bookkeeping for rows that predate sync, (3) PULL — but only when the
 * account's change marker moved (or periodically as a safety net) — applying
 * remote records strictly newer than the local copy (Last-Write-Wins; tombstones
 * delete locally), then (4) PUSH the rows still marked dirty and bump the marker.
 * Pull-before-push guarantees an older local edit can never clobber a newer
 * remote one.
 *
 * Before the marker, every 20-second cycle downloaded every collection in full;
 * now an idle cycle is a single tiny request.
 */
class SyncEngine(
    private val auth: AuthRepository,
    private val remote: RemoteStore,
    private val tracker: SyncTracker,
    private val collections: List<SyncCollection>,
    private val nowMillis: () -> Long,
    private val newRevision: () -> String = { "${nowMillis()}-${Random.nextLong().toULong().toString(36)}" }
) {
    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private val mutex = Mutex()

    // What the last full pull saw (guarded by [mutex]).
    private var pulledForUid: String? = null
    private var pulledRevision: String? = null
    private var lastFullPullAt: Long = 0L

    val isConfigured: Boolean get() = FirebaseConfig.isConfigured

    /**
     * Run a sync cycle. Safe to call often: overlapping calls are serialized by
     * a mutex and it is a no-op when unconfigured or signed out.
     *
     * @param forceFullPull download every collection even if the change marker
     *   did not move (the "Sync now" button).
     */
    suspend fun syncAll(forceFullPull: Boolean = false): Result<Unit> {
        if (!FirebaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("Firebase is not configured"))
        }
        val uid = auth.uid() ?: return Result.failure(IllegalStateException("Signed out"))

        return mutex.withLock {
            _status.value = _status.value.copy(running = true, lastError = null)
            val result = runCatching {
                // Null only when the server rejected the session; being offline
                // throws an IOException instead (reported as ERROR_OFFLINE).
                val token = auth.validIdToken() ?: throw SessionExpiredException()
                val accountSwitched = tracker.claimFor(uid)
                for (c in collections) seed(c)

                // Read the marker *before* pulling: a write that lands while we pull
                // bumps it again, so the next cycle pulls once more — nothing is missed.
                val revision = remote.readRevision(uid, token)
                val now = nowMillis()
                val mustPull = forceFullPull || accountSwitched || pulledForUid != uid ||
                    revision != pulledRevision || now - lastFullPullAt >= FULL_PULL_INTERVAL_MS
                if (mustPull) {
                    for (c in collections) pull(c, uid, token)
                    pulledForUid = uid
                    pulledRevision = revision
                    lastFullPullAt = now
                }

                var pushedAny = false
                for (c in collections) {
                    if (push(c, uid, token)) pushedAny = true
                }
                // Tell the other devices something changed (after the records are up).
                if (pushedAny) remote.writeRevision(uid, newRevision(), token)
            }
            _status.value = if (result.isSuccess) {
                _status.value.copy(running = false, lastSyncedAt = nowMillis(), lastError = null)
            } else {
                _status.value.copy(running = false, lastError = describeError(result.exceptionOrNull()))
            }
            result
        }
    }

    /** What [SyncStatus.lastError] shows: a sentinel the UI localizes, or the message. */
    private fun describeError(e: Throwable?): String = when (e) {
        is SessionExpiredException -> ERROR_SESSION_EXPIRED
        // No connection / DNS failure / timeout — from the token refresh or the database.
        is IOException -> ERROR_OFFLINE
        else -> e?.message ?: "Sync failed"
    }

    /**
     * Track any local rows that existed before sync was ever enabled. They get
     * the lowest real priority (updatedAt = 1, see SyncMeta.sq): uploaded if the
     * cloud doesn't have them, but an existing cloud copy of the same id wins.
     * (They used to get "now", so a fresh device could overwrite newer cloud
     * edits of same-id rows such as renamed default categories.)
     */
    private suspend fun seed(c: SyncCollection) {
        c.localIds().forEach { id -> tracker.seedIfAbsent(c.name, id, updatedAt = LOWEST_PRIORITY_UPDATED_AT) }
    }

    private suspend fun pull(c: SyncCollection, uid: String, token: String) {
        val docs = remote.list(uid, c.name, token)
        if (docs.isEmpty()) return
        tracker.inTransaction {
            for (doc in docs) {
                val local = entry(c.name, doc.id)
                // Skip when our local copy is the same age or newer (LWW).
                if (local != null && doc.updatedAt <= local.updatedAt) continue
                if (doc.deleted) c.localDelete(doc.id) else c.applyUpsert(doc.id, doc.fields)
                setSynced(c.name, doc.id, deleted = doc.deleted, updatedAt = doc.updatedAt)
            }
        }
    }

    /** Uploads the dirty rows of [c]; returns true when anything was written. */
    private suspend fun push(c: SyncCollection, uid: String, token: String): Boolean {
        val dirty = tracker.dirtyEntries(c.name)
        var wrote = false
        for (e in dirty) {
            if (e.deleted) {
                remote.upsert(uid, c.name, e.id, meta(e.updatedAt, deleted = true), token)
                wrote = true
            } else {
                val base = c.buildFields(e.id)
                // Dirty-but-non-deleted with no local row = inconsistent state;
                // skip rather than upload an all-default "ghost" document.
                if (base == null) {
                    tracker.clearDirty(c.name, e.id, e.updatedAt)
                    continue
                }
                remote.upsert(uid, c.name, e.id, base + meta(e.updatedAt, deleted = false), token)
                wrote = true
            }
            tracker.clearDirty(c.name, e.id, e.updatedAt)
        }
        return wrote
    }

    private fun meta(updatedAt: Long, deleted: Boolean): Map<String, FsValue> = mapOf(
        FS_META_UPDATED_AT to FsValue.Int64(updatedAt),
        FS_META_DELETED to FsValue.Bool(deleted)
    )

    companion object {
        /**
         * Safety net: pull everything at least this often even if the marker did
         * not move (e.g. another device still runs an app version without it).
         */
        const val FULL_PULL_INTERVAL_MS: Long = 10 * 60 * 1000L

        /** updatedAt for real data with the lowest priority (0 = pristine built-in). */
        const val LOWEST_PRIORITY_UPDATED_AT: Long = 1L

        /** [SyncStatus.lastError] sentinels; the Sync screen shows localized text. */
        const val ERROR_OFFLINE = "__offline__"
        const val ERROR_SESSION_EXPIRED = "__session_expired__"
    }
}

/** The server rejected the stored session (refresh token revoked or expired). */
class SessionExpiredException : IllegalStateException("Session expired — sign in again")
