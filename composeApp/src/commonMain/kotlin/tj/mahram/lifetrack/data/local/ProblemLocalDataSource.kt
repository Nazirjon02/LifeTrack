package tj.mahram.lifetrack.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemPriority
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.Problem as DbProblem
import tj.mahram.lifetrack.Problem_history as DbHistory

class ProblemLocalDataSource(private val db: AppDatabase) {

    fun getAllProblems(): Flow<List<Problem>> =
        db.problemQueries.selectAllProblems().asFlow().mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    fun getHistory(problemId: String): Flow<List<ProblemHistoryEntry>> =
        db.problemQueries.selectHistoryForProblem(problemId).asFlow().mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }

    suspend fun insert(problem: Problem): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.insertProblem(
            id = problem.id,
            title = problem.title,
            description = problem.description,
            solutions = problem.solutions,
            actionPlan = problem.actionPlan,
            priority = problem.priority.name,
            status = problem.status.name,
            category = problem.category,
            progress = problem.progress.toLong(),
            color = problem.color,
            dueDate = problem.dueDate?.toEpochMilliseconds(),
            createdAt = problem.createdAt.toEpochMilliseconds(),
            updatedAt = problem.updatedAt.toEpochMilliseconds(),
            resolvedAt = problem.resolvedAt?.toEpochMilliseconds()
        )
    }

    suspend fun updateEditable(problem: Problem): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.updateProblem(
            title = problem.title,
            description = problem.description,
            solutions = problem.solutions,
            actionPlan = problem.actionPlan,
            priority = problem.priority.name,
            category = problem.category,
            color = problem.color,
            dueDate = problem.dueDate?.toEpochMilliseconds(),
            updatedAt = problem.updatedAt.toEpochMilliseconds(),
            id = problem.id
        )
    }

    suspend fun updateStatus(
        id: String,
        status: ProblemStatus,
        resolvedAt: Instant?,
        updatedAt: Instant
    ): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.updateProblemStatus(
            status = status.name,
            resolvedAt = resolvedAt?.toEpochMilliseconds(),
            updatedAt = updatedAt.toEpochMilliseconds(),
            id = id
        )
    }

    suspend fun updateProgress(
        id: String,
        progress: Int,
        status: ProblemStatus,
        resolvedAt: Instant?,
        updatedAt: Instant
    ): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.updateProblemProgress(
            progress = progress.toLong(),
            status = status.name,
            resolvedAt = resolvedAt?.toEpochMilliseconds(),
            updatedAt = updatedAt.toEpochMilliseconds(),
            id = id
        )
    }

    suspend fun delete(id: String): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.deleteHistoryForProblem(id)
        db.problemQueries.deleteProblem(id)
    }

    suspend fun insertHistory(entry: ProblemHistoryEntry): Unit = withContext(Dispatchers.Default) {
        db.problemQueries.insertHistory(
            id = entry.id,
            problemId = entry.problemId,
            message = entry.message,
            createdAt = entry.createdAt.toEpochMilliseconds()
        )
    }

    private fun DbProblem.toDomain() = Problem(
        id = id,
        title = title,
        description = description,
        solutions = solutions,
        actionPlan = actionPlan,
        priority = runCatching { ProblemPriority.valueOf(priority) }.getOrDefault(ProblemPriority.MEDIUM),
        status = runCatching { ProblemStatus.valueOf(status) }.getOrDefault(ProblemStatus.ACTIVE),
        category = category,
        progress = progress.toInt(),
        color = color,
        dueDate = dueDate?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        resolvedAt = resolvedAt?.let { Instant.fromEpochMilliseconds(it) }
    )

    private fun DbHistory.toDomain() = ProblemHistoryEntry(
        id = id,
        problemId = problemId,
        message = message,
        createdAt = Instant.fromEpochMilliseconds(createdAt)
    )
}
