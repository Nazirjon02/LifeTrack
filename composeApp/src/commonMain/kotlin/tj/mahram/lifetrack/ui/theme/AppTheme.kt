package tj.mahram.lifetrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import tj.mahram.lifetrack.domain.model.AppTheme

// ════════════════════════════════════════════════════════════════
//  COLOR SCHEMES
// ════════════════════════════════════════════════════════════════

val DarkColorScheme = darkColorScheme(
    primary             = BrandViolet,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF2C2150),
    onPrimaryContainer  = Color(0xFFE3DCFF),
    secondary           = BrandIndigo,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFF1E1B3E),
    onSecondaryContainer= Color(0xFFCDD3FF),
    tertiary            = BrandCyan,
    onTertiary          = Color(0xFF04303A),
    tertiaryContainer   = Color(0xFF0E3C46),
    onTertiaryContainer = Color(0xFFBDF1FB),
    background          = SceneDarkBg,
    onBackground        = OnDarkHi,
    surface             = SceneDarkSurface,
    onSurface           = OnDarkHi,
    surfaceVariant      = SceneDarkSurfaceHi,
    onSurfaceVariant    = OnDarkLow,
    outline             = Color(0xFF3A3553),
    outlineVariant      = Color(0xFF26223A),
    error               = DangerV2,
    onError             = Color(0xFF3A0712),
    errorContainer      = Color(0xFF3C1622),
    onErrorContainer    = Color(0xFFFFD9DF),
    scrim               = Color(0xFF000000),
)

val LightColorScheme = lightColorScheme(
    primary             = BrandVioletDeep,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFEBE4FF),
    onPrimaryContainer  = Color(0xFF2E1065),
    secondary           = BrandIndigoDeep,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFE3E6FF),
    onSecondaryContainer= Color(0xFF1E1B4B),
    tertiary            = Color(0xFF0891B2),
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFCFF5FB),
    onTertiaryContainer = Color(0xFF053B45),
    background          = SceneLightBg,
    onBackground        = OnLightHi,
    surface             = SceneLightSurface,
    onSurface           = OnLightHi,
    surfaceVariant      = Color(0xFFECEAF7),
    onSurfaceVariant    = OnLightLow,
    outline             = Color(0xFFD9D5EA),
    outlineVariant      = Color(0xFFE7E3F3),
    error               = Color(0xFFE11D48),
    onError             = Color.White,
    errorContainer      = Color(0xFFFFE1E7),
    onErrorContainer    = Color(0xFF7F1023),
    scrim               = Color(0xFF000000),
)

// ════════════════════════════════════════════════════════════════
//  GLASS TOKENS  (the soul of the look)
// ════════════════════════════════════════════════════════════════

data class AppColors(
    val isDark: Boolean,
    val sceneBg: Color,
    // translucent card fill (vertical gradient top → bottom)
    val glassTop: Color,
    val glassBottom: Color,
    // stronger fill for hero cards / sheets
    val glassElevatedTop: Color,
    val glassElevatedBottom: Color,
    // hairline gradient border (bright top-left → faint bottom-right)
    val strokeTop: Color,
    val strokeBottom: Color,
    // brand gradients
    val brand: List<Color>,        // violet → indigo
    val brandVivid: List<Color>,   // fuchsia → violet → indigo (hero accents)
    // ambient aurora blob hues
    val aurora: List<Color>,
    // semantic
    val success: Color,
    val warning: Color,
    val info: Color,
    val danger: Color,
    // task priorities
    val priorityLow: Color,
    val priorityMedium: Color,
    val priorityHigh: Color,
    val priorityCritical: Color,
    val chartColors: List<Color>,
)

private val DarkAppColors = AppColors(
    isDark = true,
    sceneBg = SceneDarkBg,
    glassTop            = Color.White.copy(alpha = 0.10f),
    glassBottom         = Color.White.copy(alpha = 0.03f),
    glassElevatedTop    = Color.White.copy(alpha = 0.15f),
    glassElevatedBottom = Color.White.copy(alpha = 0.05f),
    strokeTop           = Color.White.copy(alpha = 0.26f),
    strokeBottom        = Color.White.copy(alpha = 0.05f),
    brand      = listOf(BrandViolet, BrandIndigo),
    brandVivid = listOf(BrandFuchsia, BrandViolet, BrandIndigo),
    aurora     = listOf(BrandViolet, BrandIndigo, BrandFuchsia, BrandCyan),
    success = SuccessV2,
    warning = WarningV2,
    info    = InfoV2,
    danger  = DangerV2,
    priorityLow      = Color(0xFF94A3B8),
    priorityMedium   = BrandSky,
    priorityHigh     = WarningV2,
    priorityCritical = DangerV2,
    chartColors = ChartColorsV2,
)

private val LightAppColors = AppColors(
    isDark = false,
    sceneBg = SceneLightBg,
    glassTop            = Color.White.copy(alpha = 0.82f),
    glassBottom         = Color.White.copy(alpha = 0.55f),
    glassElevatedTop    = Color.White.copy(alpha = 0.94f),
    glassElevatedBottom = Color.White.copy(alpha = 0.74f),
    strokeTop           = Color.White.copy(alpha = 0.92f),
    strokeBottom        = BrandVioletDeep.copy(alpha = 0.10f),
    brand      = listOf(BrandVioletDeep, BrandIndigoDeep),
    brandVivid = listOf(BrandFuchsia, BrandVioletDeep, BrandIndigoDeep),
    aurora     = listOf(BrandViolet, BrandIndigo, BrandSky, BrandFuchsia),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    info    = Color(0xFF0EA5E9),
    danger  = Color(0xFFE11D48),
    priorityLow      = Color(0xFF64748B),
    priorityMedium   = Color(0xFF2563EB),
    priorityHigh     = Color(0xFFD97706),
    priorityCritical = Color(0xFFE11D48),
    chartColors = ChartColorsV2,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

@Composable
fun LifeTrackTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val appColors = if (useDark) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography(),
            content = content
        )
    }
}

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current
