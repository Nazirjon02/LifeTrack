package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.StateFlow
import tj.mahram.lifetrack.domain.model.AuthState

interface AuthRepository {
    val state: StateFlow<AuthState>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut()

    /** The signed-in user's id, or null when signed out. */
    fun uid(): String?

    /**
     * A currently-valid Firebase id token, refreshing it first if it is close
     * to expiry. Returns null when signed out or when a refresh fails.
     */
    suspend fun validIdToken(): String?
}
