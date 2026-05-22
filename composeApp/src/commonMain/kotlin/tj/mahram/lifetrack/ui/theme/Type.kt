package tj.mahram.lifetrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AppTypography(): Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 48.sp, lineHeight = 52.sp,
        fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontSize = 38.sp, lineHeight = 42.sp,
        fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp
    ),
    displaySmall = TextStyle(
        fontSize = 30.sp, lineHeight = 36.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontSize = 28.sp, lineHeight = 34.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp, lineHeight = 28.sp,
        fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.5.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp, lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.5.sp, lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp, lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp
    ),
)
