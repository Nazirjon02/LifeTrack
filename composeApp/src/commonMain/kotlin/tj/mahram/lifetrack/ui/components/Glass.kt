package tj.mahram.lifetrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.ui.theme.appColors

// ════════════════════════════════════════════════════════════════
//  Brand brushes
// ════════════════════════════════════════════════════════════════
@Composable
fun brandGradient(): Brush = Brush.linearGradient(MaterialTheme.appColors.brand)

@Composable
fun brandVividGradient(): Brush = Brush.linearGradient(MaterialTheme.appColors.brandVivid)

@Composable
fun brandHorizontalGradient(): Brush =
    Brush.horizontalGradient(MaterialTheme.appColors.brandVivid)

// ════════════════════════════════════════════════════════════════
//  Glass surface modifier — translucent fill + light-leak + hairline
// ════════════════════════════════════════════════════════════════
@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    elevated: Boolean = false,
    glow: Color? = null,
): Modifier {
    val c = MaterialTheme.appColors
    val topFill = if (elevated) c.glassElevatedTop else c.glassTop
    val bottomFill = if (elevated) c.glassElevatedBottom else c.glassBottom
    val fill = Brush.verticalGradient(
        0.0f to (if (c.isDark) Color.White.copy(alpha = topFill.alpha + 0.06f) else topFill),
        0.10f to topFill,
        1.0f to bottomFill,
    )
    val borderBrush = Brush.linearGradient(listOf(c.strokeTop, c.strokeBottom))
    val glowAlpha = if (c.isDark) 0.30f else 0.20f
    return this
        .clip(shape)
        .background(fill, shape)
        .then(
            if (glow != null) Modifier.drawWithContent {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(glow.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(size.width * 0.28f, 0f),
                        radius = size.width * 0.85f
                    )
                )
                drawContent()
            } else Modifier
        )
        .border(1.dp, borderBrush, shape)
}

// ════════════════════════════════════════════════════════════════
//  GlassCard
// ════════════════════════════════════════════════════════════════
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevated: Boolean = false,
    glow: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = modifier
            .glassSurface(shape = shape, elevated = elevated, glow = glow)
            .then(clickMod)
            .padding(contentPadding),
        content = content
    )
}

// ════════════════════════════════════════════════════════════════
//  Ambient aurora background — slow-drifting violet/indigo glows
// ════════════════════════════════════════════════════════════════
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val c = MaterialTheme.appColors
    val t = rememberInfiniteTransition(label = "aurora")
    val p by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "auroraP"
    )
    val q by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(26000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "auroraQ"
    )
    val aur = c.aurora
    val strong = c.isDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(c.sceneBg)
                val w = size.width
                val h = size.height
                fun blob(color: Color, cx: Float, cy: Float, r: Float, a: Float) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(color.copy(alpha = a), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                }
                blob(aur[0], w * (0.16f + 0.10f * p), h * (0.06f + 0.04f * q), w * 0.95f, if (strong) 0.42f else 0.30f)
                blob(aur[1], w * (0.93f - 0.10f * q), h * (0.28f + 0.08f * p), w * 0.85f, if (strong) 0.34f else 0.24f)
                blob(aur[2], w * (0.84f - 0.08f * p), h * (0.95f - 0.05f * q), w * 1.00f, if (strong) 0.30f else 0.20f)
                if (aur.size > 3) {
                    blob(aur[3], w * (0.08f + 0.05f * q), h * (0.78f + 0.05f * p), w * 0.72f, if (strong) 0.22f else 0.16f)
                }
            },
        content = content
    )
}

// ════════════════════════════════════════════════════════════════
//  Gradient (brand) pill button with press feedback
// ════════════════════════════════════════════════════════════════
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: androidx.compose.ui.unit.Dp = 54.dp,
    leading: @Composable (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btnScale"
    )
    val brush = brandHorizontalGradient()
    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) brush else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leading?.invoke()
            Text(
                text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Section header
// ════════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        trailing?.invoke()
    }
}
