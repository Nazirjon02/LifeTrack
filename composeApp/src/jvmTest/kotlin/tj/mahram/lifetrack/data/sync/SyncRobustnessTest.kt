package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import tj.mahram.lifetrack.data.local.TransactionLocalDataSource
import tj.mahram.lifetrack.data.remote.firebase.FsValue
import tj.mahram.lifetrack.data.sync.SyncCollectionNames.TASKS
import tj.mahram.lifetrack.data.sync.SyncCollectionNames.TRANSACTIONS
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.domain.model.TransactionType

class SyncRobustnessTest {
    private val clock = TestClock()
    private val cloud = FakeCloud()

    // ── #15: offline must not look like an expired session ───────────────────

    @Test
    fun beingOfflineIsNotReportedAsAnExpiredSession() = runBlocking {
        val phone = Device(cloud, "alice", clock)

        phone.auth.tokenFailure = IOException("Unable to resolve host securetoken.googleapis.com")
        assertTrue(phone.engine.syncAll().isFailure)
        assertEquals(SyncEngine.ERROR_OFFLINE, phone.engine.status.value.lastError)

        phone.auth.tokenFailure = null
        phone.auth.sessionRejected = true
        assertTrue(phone.engine.syncAll().isFailure)
        assertEquals(SyncEngine.ERROR_SESSION_EXPIRED, phone.engine.status.value.lastError)

        phone.auth.sessionRejected = false
        assertTrue(phone.engine.syncAll().isSuccess)
        assertNull(phone.engine.status.value.lastError)
    }

    // ── #14: a value this app version doesn't know must not crash it ─────────

    @Test
    fun unknownEnumValuesFromTheCloudFallBackInsteadOfCrashing() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        // e.g. written by a newer app version that added more options
        cloud.put("alice", TASKS, "t1", mapOf(
            "id" to FsValue.Str("t1"),
            "title" to FsValue.Str("From a newer app"),
            "priority" to FsValue.Str("URGENT"),
            "recurringType" to FsValue.Str("HOURLY")
        ), updatedAt = clock.now)
        cloud.put("alice", TRANSACTIONS, "x1", mapOf(
            "id" to FsValue.Str("x1"),
            "amount" to FsValue.Dbl(5.0),
            "type" to FsValue.Str("TRANSFER"),
            "categoryId" to FsValue.Str("exp_food"),
            "recurringType" to FsValue.Str("HOURLY")
        ), updatedAt = clock.now)

        phone.sync()

        val task = phone.tasks.getAllTasks().first().single()
        assertEquals(TaskPriority.MEDIUM, task.priority)
        assertNull(task.recurringType)
        val txn = TransactionLocalDataSource(phone.db).getAllTransactions().first().single()
        assertEquals(TransactionType.EXPENSE, txn.type)
        assertNull(txn.recurringType)
    }
}
