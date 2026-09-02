package tj.mahram.lifetrack.data.remote.firebase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Tokens returned by Firebase Identity Toolkit / Secure Token endpoints. */
data class AuthTokens(
    val idToken: String,
    val refreshToken: String,
    val localId: String,
    val email: String,
    /** Absolute expiry time in epoch millis. */
    val expiresAtMillis: Long
)

class FirebaseAuthException(message: String) : Exception(message)

/**
 * Thin Ktor wrapper over Firebase Auth REST. Works identically on Android, iOS
 * and Desktop because it is plain HTTPS — no native Firebase SDK involved.
 */
class FirebaseAuthApi(
    private val client: HttpClient,
    private val nowMillis: () -> Long
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun signUp(email: String, password: String): AuthTokens =
        password(email, password, "https://identitytoolkit.googleapis.com/v1/accounts:signUp")

    suspend fun signIn(email: String, password: String): AuthTokens =
        password(email, password, "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword")

    private suspend fun password(email: String, password: String, url: String): AuthTokens {
        val response = client.post("$url?key=${FirebaseConfig.apiKey}") {
            contentType(ContentType.Application.Json)
            setBody(PasswordRequest(email = email, password = password))
        }
        ensureSuccess(response)
        val body = json.decodeFromString<PasswordResponse>(response.bodyAsText())
        return AuthTokens(
            idToken = body.idToken,
            refreshToken = body.refreshToken,
            localId = body.localId,
            email = body.email ?: email,
            expiresAtMillis = nowMillis() + (body.expiresIn.toLongOrNull() ?: 3600L) * 1000L
        )
    }

    /** Exchange a refresh token for a fresh id token. Keeps the same email. */
    suspend fun refresh(refreshToken: String, email: String): AuthTokens {
        val response = client.post("https://securetoken.googleapis.com/v1/token?key=${FirebaseConfig.apiKey}") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("grant_type", "refresh_token")
                        append("refresh_token", refreshToken)
                    }
                )
            )
        }
        ensureSuccess(response)
        val body = json.decodeFromString<RefreshResponse>(response.bodyAsText())
        return AuthTokens(
            idToken = body.idToken,
            refreshToken = body.refreshToken,
            localId = body.userId,
            email = email,
            expiresAtMillis = nowMillis() + (body.expiresIn.toLongOrNull() ?: 3600L) * 1000L
        )
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (response.status.isSuccess()) return
        val raw = runCatching { response.bodyAsText() }.getOrDefault("")
        val message = runCatching {
            json.decodeFromString<ErrorEnvelope>(raw).error?.message
        }.getOrNull()
        throw FirebaseAuthException(mapAuthError(message) ?: "Auth failed (${response.status.value})")
    }

    private fun mapAuthError(code: String?): String? = when (code) {
        null -> null
        "EMAIL_EXISTS" -> "This email is already registered."
        "EMAIL_NOT_FOUND" -> "No account found for this email."
        "INVALID_PASSWORD", "INVALID_LOGIN_CREDENTIALS" -> "Wrong email or password."
        "INVALID_EMAIL" -> "Invalid email address."
        "WEAK_PASSWORD : Password should be at least 6 characters" -> "Password must be at least 6 characters."
        "USER_DISABLED" -> "This account has been disabled."
        "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many attempts. Try again later."
        else -> code.substringBefore(" :").replace('_', ' ').lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    @Serializable
    private data class PasswordRequest(
        val email: String,
        val password: String,
        val returnSecureToken: Boolean = true
    )

    @Serializable
    private data class PasswordResponse(
        val idToken: String,
        val refreshToken: String,
        val localId: String,
        val email: String? = null,
        val expiresIn: String = "3600"
    )

    @Serializable
    private data class RefreshResponse(
        @SerialName("id_token") val idToken: String,
        @SerialName("refresh_token") val refreshToken: String,
        @SerialName("user_id") val userId: String,
        @SerialName("expires_in") val expiresIn: String = "3600"
    )

    @Serializable private data class ErrorEnvelope(val error: ErrorBody? = null)
    @Serializable private data class ErrorBody(val code: Int = 0, val message: String? = null)
}
