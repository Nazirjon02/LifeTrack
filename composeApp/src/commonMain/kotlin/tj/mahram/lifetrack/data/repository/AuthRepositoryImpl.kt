package tj.mahram.lifetrack.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tj.mahram.lifetrack.data.remote.firebase.AuthTokens
import tj.mahram.lifetrack.data.remote.firebase.FirebaseAuthApi
import tj.mahram.lifetrack.domain.model.AuthState
import tj.mahram.lifetrack.domain.repository.AuthRepository

/**
 * Persists Firebase auth tokens in multiplatform-settings and exposes a live
 * [AuthState]. All Firestore requests go through [validIdToken], which
 * transparently refreshes an expired id token.
 */
class AuthRepositoryImpl(
    private val api: FirebaseAuthApi,
    private val settings: Settings,
    private val nowMillis: () -> Long
) : AuthRepository {

    companion object {
        private const val KEY_REFRESH = "auth_refresh_token"
        private const val KEY_ID_TOKEN = "auth_id_token"
        private const val KEY_EXPIRY = "auth_id_token_expiry"
        private const val KEY_UID = "auth_uid"
        private const val KEY_EMAIL = "auth_email"
        // refresh a bit before the real expiry to avoid edge-of-expiry failures
        private const val EXPIRY_SKEW_MS = 60_000L
    }

    private val refreshMutex = Mutex()

    private val _state = MutableStateFlow(loadState())
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    private fun loadState(): AuthState {
        val uid = settings.getStringOrNull(KEY_UID)
        val email = settings.getStringOrNull(KEY_EMAIL)
        val refresh = settings.getStringOrNull(KEY_REFRESH)
        return if (uid != null && email != null && refresh != null) {
            AuthState.SignedIn(uid, email)
        } else {
            AuthState.SignedOut
        }
    }

    private fun persist(tokens: AuthTokens) {
        settings.putString(KEY_REFRESH, tokens.refreshToken)
        settings.putString(KEY_ID_TOKEN, tokens.idToken)
        settings.putLong(KEY_EXPIRY, tokens.expiresAtMillis)
        settings.putString(KEY_UID, tokens.localId)
        settings.putString(KEY_EMAIL, tokens.email)
        _state.value = AuthState.SignedIn(tokens.localId, tokens.email)
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        persist(api.signIn(email.trim(), password))
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        persist(api.signUp(email.trim(), password))
    }

    override suspend fun signOut() {
        settings.remove(KEY_REFRESH)
        settings.remove(KEY_ID_TOKEN)
        settings.remove(KEY_EXPIRY)
        settings.remove(KEY_UID)
        settings.remove(KEY_EMAIL)
        _state.value = AuthState.SignedOut
    }

    override fun uid(): String? = settings.getStringOrNull(KEY_UID)

    override suspend fun validIdToken(): String? {
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        val cached = settings.getStringOrNull(KEY_ID_TOKEN)
        val expiry = settings.getLong(KEY_EXPIRY, 0L)
        if (cached != null && nowMillis() < expiry - EXPIRY_SKEW_MS) return cached

        return refreshMutex.withLock {
            // Re-check inside the lock: another caller may have refreshed already.
            val freshCached = settings.getStringOrNull(KEY_ID_TOKEN)
            val freshExpiry = settings.getLong(KEY_EXPIRY, 0L)
            if (freshCached != null && nowMillis() < freshExpiry - EXPIRY_SKEW_MS) return@withLock freshCached

            val email = settings.getStringOrNull(KEY_EMAIL) ?: ""
            runCatching { api.refresh(refresh, email) }
                .onSuccess { persist(it) }
                .map { it.idToken }
                .getOrNull()
        }
    }
}
