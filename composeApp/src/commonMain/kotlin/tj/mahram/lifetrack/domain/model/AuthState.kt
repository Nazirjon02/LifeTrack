package tj.mahram.lifetrack.domain.model

/** Current cloud-account session state. */
sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(val uid: String, val email: String) : AuthState
}
