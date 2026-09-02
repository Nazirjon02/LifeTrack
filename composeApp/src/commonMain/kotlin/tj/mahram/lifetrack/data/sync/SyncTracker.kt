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

    suspend fun setSynced(collection: String, id: String, deleted: Boolean, updatedAt: Long): Unit =
        withContext(Dispatchers.Default) {
            q.setSynced(collection, id, if (deleted) 1L else 0L, updatedAt)
        }

    suspend fun seedIfAbsent(collection: String, id: String, updatedAt: Long): Unit =
        withContext(Dispatchers.Default) {
            q.seedIfAbsent(collection, id, updatedAt)
        }

    suspend fun dirtyEntries(collection: String): List<SyncMetaEntry> = withContext(Dispatchers.Default) {
        q.selectDirty(collection).executeAsList().map { SyncMetaEntry(it.id, it.deleted == 1L, it.updatedAt) }
    }

    suspend fun entry(collection: String, id: String): SyncMetaEntry? = withContext(Dispatchers.Default) {
        q.selectEntry(collection, id).executeAsOneOrNull()?.let {
            SyncMetaEntry(it.id, it.deleted == 1L, it.updatedAt)
        }
    }

    /** Wipe all sync bookkeeping (used on sign-out so a new account re-seeds). */
    suspend fun clearAll(): Unit = withContext(Dispatchers.Default) { q.deleteAll() }
}
