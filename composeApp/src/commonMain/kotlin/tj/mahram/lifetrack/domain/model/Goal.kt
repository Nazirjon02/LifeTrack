package tj.mahram.lifetrack.domain.model

import kotlinx.datetime.Instant

data class Goal(
    val id: String,
    val title: String,
    val description: String?,
    val icon: String,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val color: String,
    val deadline: Instant?,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val progressFraction: Float
        get() = if (targetValue > 0) (currentValue / targetValue).toFloat().coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}
