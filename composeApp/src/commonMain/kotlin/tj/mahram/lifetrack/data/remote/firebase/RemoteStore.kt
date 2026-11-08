package tj.mahram.lifetrack.data.remote.firebase

/**
 * The cloud side of sync, as the [tj.mahram.lifetrack.data.sync.SyncEngine] sees
 * it. Implemented by [RealtimeDbApi]; tests use an in-memory fake.
 */
interface RemoteStore {
    /** Create or overwrite one record of [collection]. */
    suspend fun upsert(uid: String, collection: String, docId: String, fields: Map<String, FsValue>, idToken: String)

    /** Every record of [collection] (tombstones included). */
    suspend fun list(uid: String, collection: String, idToken: String): List<RemoteDoc>

    /**
     * The account's change marker: a tiny value every device rewrites after it
     * uploads changes. Polling it tells whether anything changed without
     * downloading the collections. Null when no device has written one yet.
     */
    suspend fun readRevision(uid: String, idToken: String): String?

    suspend fun writeRevision(uid: String, revision: String, idToken: String)
}
