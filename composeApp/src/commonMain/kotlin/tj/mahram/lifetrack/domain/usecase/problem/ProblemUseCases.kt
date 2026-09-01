package tj.mahram.lifetrack.domain.usecase.problem

import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.repository.ProblemRepository

class GetAllProblemsUseCase(private val repository: ProblemRepository) {
    operator fun invoke(): Flow<List<Problem>> = repository.getAllProblems()
}

class GetProblemHistoryUseCase(private val repository: ProblemRepository) {
    operator fun invoke(problemId: String): Flow<List<ProblemHistoryEntry>> =
        repository.getHistory(problemId)
}

class CreateProblemUseCase(private val repository: ProblemRepository) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        solutions: String?,
        actionPlan: String?,
        priority: ProblemPriority,
        category: String,
        color: String,
        dueDate: Instant?,
        historyMessage: String
    ) {
        val now = Clock.System.now()
        val id = now.toEpochMilliseconds().toString()
        repository.createProblem(
            Problem(
                id = id,
                title = title.trim(),
                description = description?.trim()?.ifEmpty { null },
                solutions = solutions?.trim()?.ifEmpty { null },
                actionPlan = actionPlan?.trim()?.ifEmpty { null },
                priority = priority,
                status = ProblemStatus.ACTIVE,
                category = category.trim(),
                progress = 0,
                color = color,
                dueDate = dueDate,
                createdAt = now,
                updatedAt = now,
                resolvedAt = null
            )
        )
        repository.recordHistory(id, historyMessage)
    }
}

class UpdateProblemUseCase(private val repository: ProblemRepository) {
    /** [edited] carries the id + all editable fields. Status/progress unchanged. */
    suspend operator fun invoke(edited: Problem, historyMessage: String) {
        repository.updateProblem(edited.copy(updatedAt = Clock.System.now()))
        repository.recordHistory(edited.id, historyMessage)
    }
}

class UpdateProblemStatusUseCase(private val repository: ProblemRepository) {
    suspend operator fun invoke(problemId: String, status: ProblemStatus, historyMessage: String) {
        val now = Clock.System.now()
        val resolvedAt = if (status == ProblemStatus.RESOLVED) now else null
        repository.updateStatus(problemId, status, resolvedAt, now)
        repository.recordHistory(problemId, historyMessage)
    }
}

class UpdateProblemProgressUseCase(private val repository: ProblemRepository) {
    /** Derives status from the new progress: 100 → resolved, >0 → in progress, 0 → active. */
    suspend operator fun invoke(problemId: String, progress: Int, historyMessage: String) {
        val now = Clock.System.now()
        val clamped = progress.coerceIn(0, 100)
        val status = when {
            clamped >= 100 -> ProblemStatus.RESOLVED
            clamped > 0    -> ProblemStatus.IN_PROGRESS
            else           -> ProblemStatus.ACTIVE
        }
        val resolvedAt = if (status == ProblemStatus.RESOLVED) now else null
        repository.updateProgress(problemId, clamped, status, resolvedAt, now)
        repository.recordHistory(problemId, historyMessage)
    }
}

class DeleteProblemUseCase(private val repository: ProblemRepository) {
    suspend operator fun invoke(problemId: String) = repository.deleteProblem(problemId)
}
