package tj.mahram.lifetrack.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/** Meta field names stored alongside the entity's own columns in each record. */
const val FS_META_UPDATED_AT = "_updatedAt"
const val FS_META_DELETED = "_deleted"

/** A record read back from the Realtime Database. */
class RemoteDoc(
    val id: String,
    val fields: FsFields,
    val updatedAt: Long,
    val deleted: Boolean
)

class RemoteStoreException(message: String) : Exception(message)

/**
 * Minimal Firebase Realtime Database REST client scoped to one user's tree at
 * `users/{uid}/{collection}/{id}`. Auth is the Firebase id token passed as the
 * `?auth=` query parameter (the documented REST method for ID tokens; the call
 * is HTTPS so it is not sent in clear text). Cross-platform: plain HTTPS.
 */
class RealtimeDbApi(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun recordUrl(uid: String, collection: String, id: String, token: String) =
        "${FirebaseConfig.databaseUrl}/users/$uid/$collection/$id.json?auth=$token"

    private fun collectionUrl(uid: String, collection: String, token: String) =
        "${FirebaseConfig.databaseUrl}/users/$uid/$collection.json?auth=$token"

    /** Create or overwrite a record (PUT replaces the whole node). */
    suspend fun upsert(
        uid: String,
        collection: String,
        docId: String,
        fields: Map<String, FsValue>,
        idToken: String
    ) {
        val body = buildJsonObject {
            fields.forEach { (name, value) -> put(name, value.toJson()) }
        }
        val response = client.put(recordUrl(uid, collection, docId, idToken)) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), body))
        }
        ensureSuccess(response, "upsert $collection/$docId")
    }

    /** Read every record in the user's collection. Returns [] when empty. */
    suspend fun list(uid: String, collection: String, idToken: String): List<RemoteDoc> {
        val response = client.get(collectionUrl(uid, collection, idToken))
        ensureSuccess(response, "list $collection")
        val root = json.parseToJsonElement(response.bodyAsText())
        if (root is JsonNull) return emptyList()
        val obj = root as? JsonObject ?: return emptyList()
        return obj.mapNotNull { (id, node) ->
            val recordObj = node as? JsonObject ?: return@mapNotNull null
            val fields = FsFields.fromJsonObject(recordObj)
            RemoteDoc(
                id = id,
                fields = fields,
                updatedAt = fields.long(FS_META_UPDATED_AT) ?: 0L,
                deleted = fields.bool(FS_META_DELETED)
            )
        }
    }

    private suspend fun ensureSuccess(response: HttpResponse, what: String) {
        if (response.status.isSuccess()) return
        val raw = runCatching { response.bodyAsText() }.getOrDefault("")
        throw RemoteStoreException("Realtime DB $what failed (${response.status.value}): ${raw.take(300)}")
    }
}
