package tj.mahram.lifetrack.domain.model

import kotlin.time.Instant

/**
 * Sample tasks a brand-new user starts with. They use fixed ids so the copies
 * created on different devices are the same records in the cloud — signing in
 * on a second device can't add another seven.
 */
object SampleTasks {
    private val samples = listOf(
        Triple("Deep work session 🎯",    TaskPriority.CRITICAL, "Focus on your most important project"),
        Triple("Morning workout 💪",       TaskPriority.HIGH,     "Start the day strong and energized"),
        Triple("Plan tomorrow's goals 📋", TaskPriority.HIGH,     "End the day with tomorrow clarified"),
        Triple("Read for 30 minutes 📚",   TaskPriority.MEDIUM,   "Expand your knowledge daily"),
        Triple("Review notes & ideas 📝",  TaskPriority.MEDIUM,   "Keep your projects on track"),
        Triple("Meditate 10 minutes 🧘",   TaskPriority.LOW,      "Clear your mind and reduce stress"),
        Triple("Connect with a friend 🤝", TaskPriority.LOW,      "Relationships are your superpower"),
    )

    fun build(now: Instant): List<Task> = samples.mapIndexed { index, (title, priority, description) ->
        Task(
            id = "sample-task-${index + 1}",
            title = title,
            description = description,
            priority = priority,
            categoryId = null,
            isCompleted = false,
            dueDate = now,
            createdAt = now,
            updatedAt = now,
            isRecurring = false,
            recurringType = null,
            parentTaskId = null
        )
    }
}
