package tj.mahram.lifetrack.data.remote.firebase

/**
 * Firebase project coordinates. Fill these in from
 * Firebase console → Project settings → General.
 *
 *  - [projectId]  : the Firestore project id (e.g. "lifetrack-1a2b3")
 *  - [apiKey]     : the "Web API Key" used by the Identity Toolkit REST auth
 *
 * These are NOT secrets — the Web API key is safe to ship in a client; access
 * is controlled by Firebase Security Rules, not by hiding the key.
 */
object FirebaseConfig {
    const val projectId: String = "lifetrack-3d5bc"
    const val apiKey: String = "AIzaSyBrHxMonvr9DpoR1H__KZvuKHYRFEHmD-E"

    /** Realtime Database base URL (no trailing slash). */
    const val databaseUrl: String = "https://lifetrack-3d5bc-default-rtdb.firebaseio.com"

    /** True once real values have been provided, so sync stays a no-op until then. */
    val isConfigured: Boolean
        get() = apiKey.isNotBlank() &&
            projectId.isNotBlank() &&
            databaseUrl.startsWith("https://")
}
