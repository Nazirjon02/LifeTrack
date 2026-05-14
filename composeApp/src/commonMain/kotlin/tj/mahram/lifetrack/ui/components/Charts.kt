package tj.mahram.lifetrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import tj.mahram.lifetrack.ui.theme.ChartColors

@Composable
fun LineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillGradient: Boolean = true,
    showDots: Boolean = false
) {
    if (data.size < 2) return
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "chart"
    )
    val minVal = data.min()
    val maxVal = data.max()
    val range = (maxVal - minVal).coerceAtLeast(0.001)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = w / (data.size - 1)
        val pointCount = (data.size * animationProgress).toInt().coerceAtLeast(2)
        val visibleData = data.take(pointCount)

        fun xFor(i: Int) = i * step
        fun yFor(v: Double) = (h - ((v - minVal) / range * h * 0.9f + h * 0.05f)).toFloat()

        val path = Path()
        visibleData.forEachIndexed { i, v ->
            if (i == 0) path.moveTo(xFor(i), yFor(v))
            else path.lineTo(xFor(i), yFor(v))
        }

        if (fillGradient) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(xFor(visibleData.size - 1), h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.4f), lineColor.copy(alpha = 0f)),
                    startY = 0f,
                    endY = h
                )
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (showDots && visibleData.size <= 30) {
            visibleData.forEachIndexed { i, v ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(xFor(i), yFor(v))
                )
            }
        }
    }
}

@Composable
fun PieChart(
    segments: List<Pair<String, Float>>,
    colors: List<Color> = ChartColors,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) return
    val total = segments.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.001f)
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700, easing = EaseOutCubic),
        label = "pie"
    )

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val r = size.minDimension / 2f * 0.9f
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        segments.forEachIndexed { i, (_, value) ->
            val sweep = (value / total) * 360f * animProgress
            drawArc(
                color = colors[i % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = 48.dp.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }

        // Inner circle
        drawCircle(
            color = androidx.compose.ui.graphics.Color(0xFF161B22),
            radius = r - 48.dp.toPx() / 2
        )
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return
    val maxVal = data.maxOf { it.second }.coerceAtLeast(0.001)
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
        label = "bar"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = w / (data.size * 1.5f)
        val gap = barWidth * 0.5f

        data.forEachIndexed { i, (_, value) ->
            val barH = (value / maxVal * h * 0.85f * animProgress).toFloat()
            val x = i * (barWidth + gap) + gap / 2
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, h - barH),
                size = Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
        }
    }
}
