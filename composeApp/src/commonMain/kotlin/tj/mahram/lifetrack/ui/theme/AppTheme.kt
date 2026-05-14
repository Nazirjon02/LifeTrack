package tj.mahram.lifetrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import tj.mahram.lifetrack.domain.model.AppTheme

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorColor,
    errorContainer = ErrorContainerDark
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight
)

data class AppColors(
    val success: Color,
    val warning: Color,
    val priorityLow: Color,
    val priorityMedium: Color,
    val priorityHigh: Color,
    val priorityCritical: Color,
    val chartColors: List<Color>
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        success = SuccessColor,
        warning = WarningColor,
        priorityLow = PriorityLow,
        priorityMedium = PriorityMedium,
        priorityHigh = PriorityHigh,
        priorityCritical = PriorityCritical,
        chartColors = ChartColors
    )
}

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

    CompositionLocalProvider(
        LocalAppColors provides AppColors(
            success = SuccessColor,
            warning = WarningColor,
            priorityLow = PriorityLow,
            priorityMedium = PriorityMedium,
            priorityHigh = PriorityHigh,
            priorityCritical = PriorityCritical,
            chartColors = ChartColors
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography(),
            content = content
        )
    }
}

val MaterialTheme.appColors: AppColors
    @Composable get() = LocalAppColors.current
