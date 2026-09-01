package tj.mahram.lifetrack.feature.problems

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tj.mahram.lifetrack.core.i18n.LocalStrings
import tj.mahram.lifetrack.domain.model.ProblemAnalytics
import tj.mahram.lifetrack.ui.components.BarChart
import tj.mahram.lifetrack.ui.components.GlassCard
import tj.mahram.lifetrack.ui.theme.appColors

@Composable
fun ProblemsAnalyticsView(state: ProblemsState) {
    val s = LocalStrings.current
    val a = state.analytics
    val c = MaterialTheme.appColors

    if (!a.hasData) {
        Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
            Text(s.analyticsNoData, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Counters
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatMini(Modifier.weight(1f), a.totalCreated.toString(), s.analyticsCreated, MaterialTheme.colorScheme.primary)
            StatMini(Modifier.weight(1f), a.totalResolved.toString(), s.analyticsResolved, c.success)
            StatMini(Modifier.weight(1f), a.totalActive.toString(), s.analyticsActiveLabel, c.danger)
        }

        // Resolution rates
        GlassCard(shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(18.dp)) {
            Text(s.analyticsResolutionRate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(14.dp))
            RateBar(label = s.analyticsThisWeek, rate = a.weeklyRate, resolved = a.resolvedThisWeek, created = a.createdThisWeek, color = c.success)
            Spacer(Modifier.height(12.dp))
            RateBar(label = s.analyticsThisMonth, rate = a.monthlyRate, resolved = a.resolvedThisMonth, created = a.createdThisMonth, color = MaterialTheme.colorScheme.primary)
        }

        // Progress chart (resolved per day, last 7 days)
        GlassCard(shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(18.dp)) {
            Text(s.analyticsProgressTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(14.dp))
            BarChart(
                data = a.resolvedPerDay.map { it.first to it.second.toDouble() },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                barColor = c.success
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                a.resolvedPerDay.forEach { (label, _) ->
                    Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }

        // Top categories
        GlassCard(shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(18.dp)) {
            Text(s.analyticsTopCategories, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            val maxCount = (a.topCategories.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                a.topCategories.forEach { (cat, count) ->
                    CategoryBar(cat, count, maxCount)
                }
            }
        }
    }
}

@Composable
private fun StatMini(modifier: Modifier, value: String, label: String, accent: Color) {
    GlassCard(modifier = modifier.height(100.dp), shape = RoundedCornerShape(20.dp), glow = accent, contentPadding = PaddingValues(14.dp)) {
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = accent, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun RateBar(label: String, rate: Float, resolved: Int, created: Int, color: Color) {
    val anim by animateFloatAsState(rate, tween(900, easing = FastOutSlowInEasing), label = "rate_$label")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("$resolved / $created · ${(anim * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
        }
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.15f))) {
            Box(modifier = Modifier.fillMaxWidth(anim).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))))
        }
    }
}

@Composable
private fun CategoryBar(category: String, count: Int, maxCount: Int) {
    val frac = (count.toFloat() / maxCount).coerceIn(0.04f, 1f)
    val anim by animateFloatAsState(frac, tween(800, easing = FastOutSlowInEasing), label = "cat_$category")
    val color = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
            Text("$count", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Box(modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.12f))) {
            Box(modifier = Modifier.fillMaxWidth(anim).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.55f)))))
        }
    }
}
