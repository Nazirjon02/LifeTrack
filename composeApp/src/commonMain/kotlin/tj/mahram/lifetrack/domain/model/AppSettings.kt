package tj.mahram.lifetrack.domain.model

data class AppSettings(
    val theme: AppTheme,
    val currency: String,
    val language: AppLanguage,
    val notificationsEnabled: Boolean,
    val taskNotificationsEnabled: Boolean,
    val financeNotificationsEnabled: Boolean,
    val cryptoNotificationsEnabled: Boolean,
    /** Money the user already had before tracking anything in the app. */
    val openingBalance: Double = 0.0
)

enum class AppTheme(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский")
}

val SupportedCurrencies = listOf("USD", "EUR", "RUB", "TJS", "GBP", "JPY")
