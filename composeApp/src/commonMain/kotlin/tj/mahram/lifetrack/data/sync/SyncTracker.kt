package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tj.mahram.lifetrack.data.local.db.AppDatabase

/** A decoded row of the sync_meta table. */
data class SyncMetaEntry(val id: String, val deleted: Boolean, val updatedAt: Long)

/**
 * Reads and writes the sync_meta bookkeeping table. Repositories call
 * [markDirty] / [markDeleted] after every local mutation; the sync engine uses
 * the rest to drive push/pull and Last-Write-Wins.
 */
class SyncTracker(
    private val db: AppDatabase,
    private val nowMillis: () -> Long
) {
    private val q get() = db.syncMetaQueries

    suspend fun markDirty(collection: String, id: String): Unit = withContext(Dispatchers.Default) {
        q.markDirty(collection, id, nowMillis())
    }

    suspend fun markDeleted(collection: String, id: String): Unit = withContext(Dispatchers.Default) {
        q.markDeleted(collection, id, nowMillis())
    }

    suspend fun clearDirty(collection: String, id: String, updatedAt: Long): Unit =
        withContext(Dispatchers.Default) {
            q.clearDirty(collection, id, updatedAt)
        }

    suspend fun seedIfAbsent(collection: String, id: String, updatedAt: Long): Unit =
        withContext(Dispatchers.Default) {
            q.seedIfAbsent(collection, id, updatedAt)
        }

    /**
     * Track built-in rows that every device creates with the same id (default
     * categories, sample tasks) as already synced at updatedAt = 0: they are not
     * uploaded until the user edits them, and the account's own copy always wins
     * on pull — so a fresh install can never overwrite or duplicate them.
     */
    suspend fun markPristine(collection: String, ids: List<String>): Unit = withContext(Dispatchers.Default) {
        db.transaction { ids.forEach { q.markPristine(collection, it) } }
    }

    suspend fun dirtyEntries(collection: String): List<SyncMetaEntry> = withContext(Dispatchers.Default) {
        q.selectDirty(collection).executeAsList().map { SyncMetaEntry(it.id, it.deleted == 1L, it.updatedAt) }
    }

    suspend fun entry(collection: String, id: String): SyncMetaEntry? = withContext(Dispatchers.Default) {
        blocking.entry(collection, id)
    }

    /**
     * Makes sure the bookkeeping belongs to the signed-in account [uid].
     *
     * The first account seen simply adopts the existing bookkeeping. When a
     * *different* account signs in on this device, the bookkeeping is reset so
     * the local data merges into the new account at the lowest priority (see
     * `requeueForAccountSwitch` in SyncMeta.sq) instead of staying half-synced
     * with the previous one. The previous account's cloud data is not touched.
     *
     * @return true when the bookkeeping was reset for a new account.
     */
    suspend fun claimFor(uid: String): Boolean = withContext(Dispatchers.Default) {
        db.transactionWithResult {
            when (q.selectOwner().executeAsOneOrNull()) {
                uid -> false
                null -> {
                    q.insertOwner(uid)
                    false
                }
                else -> {
                    q.dropTombstonesForAccountSwitch()
                    q.requeueForAccountSwitch()
                    q.deleteOwner()
                    q.insertOwner(uid)
                    true
                }
            }
        }
    }

    /**
     * Runs [body] as a single DB transaction on a background thread: applying a
     * pulled collection is much faster that way, and the UI refreshes once per
     * table instead of once per row. Inside, use only the synchronous [Blocking]
     * calls and other non-suspending database work.
     */
    suspend fun <T> inTransaction(body: Blocking.() -> T): T = withContext(Dispatchers.Default) {
        db.transactionWithResult { blocking.body() }
    }

    /** Synchronous bookkeeping calls for use inside [inTransaction]. */
    inner class Blocking internal constructor() {
        fun entry(collection: String, id: String): SyncMetaEntry? =
            q.selectEntry(collection, id).executeAsOneOrNull()?.let {
                SyncMetaEntry(it.id, it.deleted == 1L, it.updatedAt)
            }

        fun setSynced(collection: String, id: String, deleted: Boolean, updatedAt: Long) {
            q.setSynced(collection, id, if (deleted) 1L else 0L, updatedAt)
        }
    }

    private val blocking = Blocking()
}
