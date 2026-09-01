package tj.mahram.lifetrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.Problem
import tj.mahram.lifetrack.domain.model.ProblemHistoryEntry
import tj.mahram.lifetrack.domain.model.ProblemStatus

interface ProblemRepository {
    fun getAllProblems(): Flow<List<Problem>>
    fun getHistory(problemId: String): Flow<List<ProblemHistoryEntry>>

    suspend fun createProblem(problem: Problem)
    suspend fun updateProblem(problem: Problem)
    suspend fun updateStatus(problemId: String, status: ProblemStatus, resolvedAt: Instant?, updatedAt: Instant)
    suspend fun updateProgress(problemId: String, progress: Int, status: ProblemStatus, resolvedAt: Instant?, updatedAt: Instant)
    suspend fun deleteProblem(problemId: String)

    /** Append a change-log entry for [problemId]. */
    suspend fun recordHistory(problemId: String, message: String)
}
