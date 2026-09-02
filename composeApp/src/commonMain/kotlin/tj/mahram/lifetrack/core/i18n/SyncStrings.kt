package tj.mahram.lifetrack.core.i18n

import tj.mahram.lifetrack.domain.model.AppLanguage

/**
 * Localized strings for the Account & Sync screen. Kept as its own data class
 * (not folded into AppStrings) so its generated constructor stays tiny — see
 * the AppStrings 255-argument Dalvik limit note in the project memory.
 */
data class SyncStrings(
    val entryTitle: String,
    val entrySubtitle: String,
    val screenTitle: String,
    val headerSignedOut: String,
    val explainer: String,
    val emailLabel: String,
    val emailHint: String,
    val passwordLabel: String,
    val passwordHint: String,
    val signIn: String,
    val signUp: String,
    val toggleToSignUp: String,
    val toggleToSignIn: String,
    val signedInAs: String,
    val signOut: String,
    val syncNow: String,
    val syncing: String,
    val lastSynced: String,
    val never: String,
    val statusIdle: String,
    val notConfigured: String,
    val genericError: String,
    val emptyFields: String
) {
    fun lastSyncedText(value: String): String = "$lastSynced $value"
    fun signedInAsText(email: String): String = "$signedInAs $email"
}

private val SyncStringsEn = SyncStrings(
    entryTitle = "Account & Sync",
    entrySubtitle = "Back up and sync across your devices",
    screenTitle = "Account & Sync",
    headerSignedOut = "Sign in to sync",
    explainer = "Sign in with the same account on your phone, desktop and iPad to keep everything in sync. Your data stays on this device too and works offline.",
    emailLabel = "Email",
    emailHint = "you@example.com",
    passwordLabel = "Password",
    passwordHint = "At least 6 characters",
    signIn = "Sign in",
    signUp = "Create account",
    toggleToSignUp = "No account? Create one",
    toggleToSignIn = "Already have an account? Sign in",
    signedInAs = "Signed in as",
    signOut = "Sign out",
    syncNow = "Sync now",
    syncing = "Syncing…",
    lastSynced = "Last synced:",
    never = "never",
    statusIdle = "Up to date",
    notConfigured = "Firebase is not configured yet.",
    genericError = "Something went wrong. Please try again.",
    emptyFields = "Enter your email and password."
)

private val SyncStringsRu = SyncStrings(
    entryTitle = "Аккаунт и синхронизация",
    entrySubtitle = "Резервная копия и синхронизация между устройствами",
    screenTitle = "Аккаунт и синхронизация",
    headerSignedOut = "Войдите для синхронизации",
    explainer = "Войдите под одним аккаунтом на телефоне, компьютере и планшете — и всё будет синхронизироваться. Данные также хранятся на этом устройстве и работают офлайн.",
    emailLabel = "Электронная почта",
    emailHint = "you@example.com",
    passwordLabel = "Пароль",
    passwordHint = "Минимум 6 символов",
    signIn = "Войти",
    signUp = "Создать аккаунт",
    toggleToSignUp = "Нет аккаунта? Создайте",
    toggleToSignIn = "Уже есть аккаунт? Войти",
    signedInAs = "Вы вошли как",
    signOut = "Выйти",
    syncNow = "Синхронизировать",
    syncing = "Синхронизация…",
    lastSynced = "Последняя синхронизация:",
    never = "никогда",
    statusIdle = "Всё синхронизировано",
    notConfigured = "Firebase ещё не настроен.",
    genericError = "Что-то пошло не так. Попробуйте ещё раз.",
    emptyFields = "Введите почту и пароль."
)

fun syncStringsFor(language: AppLanguage): SyncStrings = when (language) {
    AppLanguage.RUSSIAN -> SyncStringsRu
    else -> SyncStringsEn
}
