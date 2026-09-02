package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tj.mahram.lifetrack.data.remote.firebase.FS_META_DELETED
import tj.mahram.lifetrack.data.remote.firebase.FS_META_UPDATED_AT
import tj.mahram.lifetrack.data.remote.firebase.FirebaseConfig
import tj.mahram.lifetrack.data.remote.firebase.FsValue
import tj.mahram.lifetrack.data.remote.firebase.RealtimeDbApi
import tj.mahram.lifetrack.domain.repository.AuthRepository

/** UI-facing snapshot of sync progress. */
data class SyncStatus(
    val running: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastError: String? = null
)

/**
 * Offline-first, per-record cloud sync over the Firestore REST API.
 *
 * For every collection we: (1) seed sync bookkeeping for rows that predate
 * sync, (2) PULL remote docs and apply any that are strictly newer than our
 * local copy (Last-Write-Wins; tombstones delete locally), then (3) PUSH the
 * rows still marked dirty. Pull-before-push guarantees an older local edit can
 * never clobber a newer remote one.
 */
class SyncEngine(
    private val auth: AuthRepository,
    private val remote: RealtimeDbApi,
    private val tracker: SyncTracker,
    private val collections: List<SyncCollection>,
    private val nowMillis: () -> Long
) {
    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private val mutex = Mutex()

    val isConfigured: Boolean get() = FirebaseConfig.isConfigured

    /**
     * Run a full sync cycle. Safe to call often: overlapping calls are
     * serialized by a mutex and it is a no-op when unconfigured or signed out.
     */
    suspend fun syncAll(): Result<Unit> {
        if (!FirebaseConfig.isConfigured) {
            return Result.failure(IllegalStateException("Firebase is not configured"))
        }
        val uid = auth.uid() ?: return Result.failure(IllegalStateException("Signed out"))

        return mutex.withLock {
            _status.value = _status.value.copy(running = true, lastError = null)
            val result = runCatching {
                val token = auth.validIdToken()
                    ?: throw IllegalStateException("Session expired — sign in again")
                for (c in collections) {
                    seed(c)
                    pull(c, uid, token)
                    push(c, uid, token)
                }
            }
            _status.value = if (result.isSuccess) {
                _status.value.copy(running = false, lastSyncedAt = nowMillis(), lastError = null)
            } else {
                _status.value.copy(running = false, lastError = result.exceptionOrNull()?.message ?: "Sync failed")
            }
            result
        }
    }

    /** Track any local rows that existed before sync was ever enabled. */
    private suspend fun seed(c: SyncCollection) {
        val now = nowMillis()
        c.localIds().forEach { id -> tracker.seedIfAbsent(c.name, id, now) }
    }

    private suspend fun pull(c: SyncCollection, uid: String, token: String) {
        val docs = remote.list(uid, c.name, token)
        for (doc in docs) {
            val local = tracker.entry(c.name, doc.id)
            // Skip when our local copy is the same age or newer (LWW).
            if (local != null && doc.updatedAt <= local.updatedAt) continue
            if (doc.deleted) {
                c.localDelete(doc.id)
                tracker.setSynced(c.name, doc.id, deleted = true, updatedAt = doc.updatedAt)
            } else {
                c.applyUpsert(doc.id, doc.fields)
                tracker.setSynced(c.name, doc.id, deleted = false, updatedAt = doc.updatedAt)
            }
        }
    }

    private suspend fun push(c: SyncCollection, uid: String, token: String) {
        val dirty = tracker.dirtyEntries(c.name)
        for (e in dirty) {
            if (e.deleted) {
                remote.upsert(uid, c.name, e.id, meta(e.updatedAt, deleted = true), token)
            } else {
                val base = c.buildFields(e.id)
                // Dirty-but-non-deleted with no local row = inconsistent state;
                // skip rather than upload an all-default "ghost" document.
                if (base == null) {
                    tracker.clearDirty(c.name, e.id, e.updatedAt)
                    continue
                }
                remote.upsert(uid, c.name, e.id, base + meta(e.updatedAt, deleted = false), token)
            }
            tracker.clearDirty(c.name, e.id, e.updatedAt)
        }
    }

    private fun meta(updatedAt: Long, deleted: Boolean): Map<String, FsValue> = mapOf(
        FS_META_UPDATED_AT to FsValue.Int64(updatedAt),
        FS_META_DELETED to FsValue.Bool(deleted)
    )
}
