package tj.mahram.lifetrack.domain.model

data class AppSettings(
    val theme: AppTheme,
    val currency: String,
    val language: AppLanguage,
    val notificationsEnabled: Boolean,
    val taskNotificationsEnabled: Boolean,
    val financeNotificationsEnabled: Boolean,
    val cryptoNotificationsEnabled: Boolean
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
