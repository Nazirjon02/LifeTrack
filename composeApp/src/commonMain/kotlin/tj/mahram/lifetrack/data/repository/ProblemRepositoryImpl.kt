package tj.mahram.lifetrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.ProblemLocalDataSource
import tj.mahram.lifetrack.data.sync.SyncCollectionNames
import tj.mahram.lifetrack.data.sync.SyncTracker
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemStatus
import tj.mahram.lifetrack.domain.repository.ProblemRepository

class ProblemRepositoryImpl(
    private val dataSource: ProblemLocalDataSource,
    private val sync: SyncTracker
) : ProblemRepository {

    override fun getAllProblems(): Flow<List<Problem>> = dataSource.getAllProblems()

    override fun getHistory(problemId: String): Flow<List<ProblemHistoryEntry>> =
        dataSource.getHistory(problemId)

    override suspend fun createProblem(problem: Problem) {
        dataSource.insert(problem)
        sync.markDirty(SyncCollectionNames.PROBLEMS, problem.id)
    }

    override suspend fun updateProblem(problem: Problem) {
        dataSource.updateEditable(problem)
        sync.markDirty(SyncCollectionNames.PROBLEMS, problem.id)
    }

    override suspend fun updateStatus(
        problemId: String,
        status: ProblemStatus,
        resolvedAt: Instant?,
        updatedAt: Instant
    ) {
        dataSource.updateStatus(problemId, status, resolvedAt, updatedAt)
        sync.markDirty(SyncCollectionNames.PROBLEMS, problemId)
    }

    override suspend fun updateProgress(
        problemId: String,
        progress: Int,
        status: ProblemStatus,
        resolvedAt: Instant?,
        updatedAt: Instant
    ) {
        dataSource.updateProgress(problemId, progress, status, resolvedAt, updatedAt)
        sync.markDirty(SyncCollectionNames.PROBLEMS, problemId)
    }

    override suspend fun deleteProblem(problemId: String) {
        dataSource.delete(problemId)
        sync.markDeleted(SyncCollectionNames.PROBLEMS, problemId)
    }

    override suspend fun recordHistory(problemId: String, message: String) {
        val now = Clock.System.now()
        val entry = ProblemHistoryEntry(
            id = "h_${now.toEpochMilliseconds()}_${problemId.takeLast(4)}",
            problemId = problemId,
            message = message,
            createdAt = now
        )
        dataSource.insertHistory(entry)
        sync.markDirty(SyncCollectionNames.PROBLEM_HISTORY, entry.id)
    }
}
