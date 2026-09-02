package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.data.remote.firebase.FsFields
import tj.mahram.lifetrack.data.remote.firebase.FsValue

/**
 * Maps one SQLDelight table to/from a Firestore document. Field names mirror
 * the column names 1:1. Reads use the generated `selectById`; writes use the
 * generated `insertOrReplace` (so SQLDelight query Flows refresh the UI) and
 * `delete` queries — always bypassing the repositories so the sync engine never
 * re-marks a freshly-pulled row as dirty.
 */
interface SyncCollection {
    val name: String
    suspend fun localIds(): List<String>
    /** Entity fields for [id], or null when the row no longer exists locally. */
    suspend fun buildFields(id: String): Map<String, FsValue>?
    suspend fun applyUpsert(id: String, f: FsFields)
    suspend fun localDelete(id: String)
}

/** All syncable collections in a stable order (parents before dependents). */
fun defaultSyncCollections(db: AppDatabase, now: () -> Long): List<SyncCollection> = listOf(
    CategorySync(db),
    TaskSync(db, now),
    TransactionSync(db, now),
    GoalSync(db, now),
    DebtSync(db, now),
    ProblemSync(db, now),
    ProblemHistorySync(db)
)

// ─────────────────────────────────────────────────────────────────────────────

private class CategorySync(private val db: AppDatabase) : SyncCollection {
    override val name = SyncCollectionNames.CATEGORIES
    private val q get() = db.categoryQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllCategoryIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectCategoryById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "name" to FsValue.of(r.name),
            "type" to FsValue.of(r.type),
            "icon" to FsValue.of(r.icon),
            "color" to FsValue.of(r.color),
            "isCustom" to FsValue.Int64(r.isCustom)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceCategory(
            id = id,
            name = f.str("name") ?: "",
            type = f.str("type") ?: "EXPENSE",
            icon = f.str("icon") ?: "",
            color = f.str("color") ?: "#7C3AED",
            isCustom = f.long("isCustom") ?: 0L
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteCategory(id) }
}

private class TaskSync(private val db: AppDatabase, private val now: () -> Long) : SyncCollection {
    override val name = SyncCollectionNames.TASKS
    private val q get() = db.taskQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllTaskIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectTaskById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "title" to FsValue.of(r.title),
            "description" to FsValue.of(r.description),
            "priority" to FsValue.of(r.priority),
            "categoryId" to FsValue.of(r.categoryId),
            "isCompleted" to FsValue.Int64(r.isCompleted),
            "dueDate" to FsValue.of(r.dueDate),
            "createdAt" to FsValue.Int64(r.createdAt),
            "updatedAt" to FsValue.Int64(r.updatedAt),
            "isRecurring" to FsValue.Int64(r.isRecurring),
            "recurringType" to FsValue.of(r.recurringType),
            "parentTaskId" to FsValue.of(r.parentTaskId)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceTask(
            id = id,
            title = f.str("title") ?: "",
            description = f.str("description"),
            priority = f.str("priority") ?: "MEDIUM",
            categoryId = f.str("categoryId"),
            isCompleted = f.long("isCompleted") ?: 0L,
            dueDate = f.long("dueDate"),
            createdAt = f.long("createdAt") ?: now(),
            updatedAt = f.long("updatedAt") ?: now(),
            isRecurring = f.long("isRecurring") ?: 0L,
            recurringType = f.str("recurringType"),
            parentTaskId = f.str("parentTaskId")
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteTask(id) }
}

private class TransactionSync(private val db: AppDatabase, private val now: () -> Long) : SyncCollection {
    override val name = SyncCollectionNames.TRANSACTIONS
    private val q get() = db.transactionRecordQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllTransactionIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectTransactionById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "amount" to FsValue.Dbl(r.amount),
            "type" to FsValue.of(r.type),
            "categoryId" to FsValue.of(r.categoryId),
            "note" to FsValue.of(r.note),
            "date" to FsValue.Int64(r.date),
            "currency" to FsValue.of(r.currency),
            "isRecurring" to FsValue.Int64(r.isRecurring),
            "recurringType" to FsValue.of(r.recurringType),
            "cryptoCoinId" to FsValue.of(r.cryptoCoinId)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceTransaction(
            id = id,
            amount = f.double("amount") ?: 0.0,
            type = f.str("type") ?: "EXPENSE",
            categoryId = f.str("categoryId") ?: "",
            note = f.str("note"),
            date = f.long("date") ?: now(),
            currency = f.str("currency") ?: "USD",
            isRecurring = f.long("isRecurring") ?: 0L,
            recurringType = f.str("recurringType"),
            cryptoCoinId = f.str("cryptoCoinId")
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteTransaction(id) }
}

private class GoalSync(private val db: AppDatabase, private val now: () -> Long) : SyncCollection {
    override val name = SyncCollectionNames.GOALS
    private val q get() = db.goalQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllGoalIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectGoalById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "title" to FsValue.of(r.title),
            "description" to FsValue.of(r.description),
            "icon" to FsValue.of(r.icon),
            "targetValue" to FsValue.Dbl(r.targetValue),
            "currentValue" to FsValue.Dbl(r.currentValue),
            "unit" to FsValue.of(r.unit),
            "color" to FsValue.of(r.color),
            "deadline" to FsValue.of(r.deadline),
            "isCompleted" to FsValue.Int64(r.isCompleted),
            "createdAt" to FsValue.Int64(r.createdAt),
            "updatedAt" to FsValue.Int64(r.updatedAt),
            "affectsBalance" to FsValue.Int64(r.affectsBalance)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceGoal(
            id = id,
            title = f.str("title") ?: "",
            description = f.str("description"),
            icon = f.str("icon") ?: "🎯",
            targetValue = f.double("targetValue") ?: 100.0,
            currentValue = f.double("currentValue") ?: 0.0,
            unit = f.str("unit") ?: "",
            color = f.str("color") ?: "#7C3AED",
            deadline = f.long("deadline"),
            isCompleted = f.long("isCompleted") ?: 0L,
            createdAt = f.long("createdAt") ?: now(),
            updatedAt = f.long("updatedAt") ?: now(),
            affectsBalance = f.long("affectsBalance") ?: 0L
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteGoal(id) }
}

private class DebtSync(private val db: AppDatabase, private val now: () -> Long) : SyncCollection {
    override val name = SyncCollectionNames.DEBTS
    private val q get() = db.debtQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllDebtIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectDebtById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "type" to FsValue.of(r.type),
            "counterparty" to FsValue.of(r.counterparty),
            "amount" to FsValue.Dbl(r.amount),
            "paidAmount" to FsValue.Dbl(r.paidAmount),
            "currency" to FsValue.of(r.currency),
            "note" to FsValue.of(r.note),
            "dueDate" to FsValue.of(r.dueDate),
            "createdAt" to FsValue.Int64(r.createdAt),
            "updatedAt" to FsValue.Int64(r.updatedAt),
            "isSettled" to FsValue.Int64(r.isSettled),
            "color" to FsValue.of(r.color)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceDebt(
            id = id,
            type = f.str("type") ?: "LENT",
            counterparty = f.str("counterparty") ?: "",
            amount = f.double("amount") ?: 0.0,
            paidAmount = f.double("paidAmount") ?: 0.0,
            currency = f.str("currency") ?: "USD",
            note = f.str("note"),
            dueDate = f.long("dueDate"),
            createdAt = f.long("createdAt") ?: now(),
            updatedAt = f.long("updatedAt") ?: now(),
            isSettled = f.long("isSettled") ?: 0L,
            color = f.str("color") ?: "#7C3AED"
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteDebt(id) }
}

private class ProblemSync(private val db: AppDatabase, private val now: () -> Long) : SyncCollection {
    override val name = SyncCollectionNames.PROBLEMS
    private val q get() = db.problemQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllProblemIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectProblemById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "title" to FsValue.of(r.title),
            "description" to FsValue.of(r.description),
            "solutions" to FsValue.of(r.solutions),
            "actionPlan" to FsValue.of(r.actionPlan),
            "priority" to FsValue.of(r.priority),
            "status" to FsValue.of(r.status),
            "category" to FsValue.of(r.category),
            "progress" to FsValue.Int64(r.progress),
            "color" to FsValue.of(r.color),
            "dueDate" to FsValue.of(r.dueDate),
            "createdAt" to FsValue.Int64(r.createdAt),
            "updatedAt" to FsValue.Int64(r.updatedAt),
            "resolvedAt" to FsValue.of(r.resolvedAt)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceProblem(
            id = id,
            title = f.str("title") ?: "",
            description = f.str("description"),
            solutions = f.str("solutions"),
            actionPlan = f.str("actionPlan"),
            priority = f.str("priority") ?: "MEDIUM",
            status = f.str("status") ?: "ACTIVE",
            category = f.str("category") ?: "",
            progress = f.long("progress") ?: 0L,
            color = f.str("color") ?: "#7C3AED",
            dueDate = f.long("dueDate"),
            createdAt = f.long("createdAt") ?: now(),
            updatedAt = f.long("updatedAt") ?: now(),
            resolvedAt = f.long("resolvedAt")
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteProblem(id) }
}

private class ProblemHistorySync(private val db: AppDatabase) : SyncCollection {
    override val name = SyncCollectionNames.PROBLEM_HISTORY
    private val q get() = db.problemQueries
    override suspend fun localIds() = withContext(Dispatchers.Default) { q.syncAllHistoryIds().executeAsList() }
    override suspend fun buildFields(id: String): Map<String, FsValue>? = withContext(Dispatchers.Default) {
        val r = q.selectHistoryById(id).executeAsOneOrNull() ?: return@withContext null
        mapOf(
            "id" to FsValue.Str(r.id),
            "problemId" to FsValue.of(r.problemId),
            "message" to FsValue.of(r.message),
            "createdAt" to FsValue.Int64(r.createdAt)
        )
    }
    override suspend fun applyUpsert(id: String, f: FsFields): Unit = withContext(Dispatchers.Default) {
        q.insertOrReplaceHistory(
            id = id,
            problemId = f.str("problemId") ?: "",
            message = f.str("message") ?: "",
            createdAt = f.long("createdAt") ?: 0L
        )
    }
    override suspend fun localDelete(id: String): Unit = withContext(Dispatchers.Default) { q.deleteHistoryById(id) }
}
