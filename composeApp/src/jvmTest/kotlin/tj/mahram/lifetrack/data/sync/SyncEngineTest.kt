package tj.mahram.lifetrack.data.sync

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import tj.mahram.lifetrack.data.remote.firebase.FS_META_DELETED
import tj.mahram.lifetrack.data.remote.firebase.FS_META_UPDATED_AT
import tj.mahram.lifetrack.data.sync.SyncCollectionNames.CATEGORIES
import tj.mahram.lifetrack.data.sync.SyncCollectionNames.TASKS
import tj.mahram.lifetrack.domain.model.SampleTasks

class SyncEngineTest {
    private val clock = TestClock()
    private val cloud = FakeCloud()

    // ── #6: no full download every cycle ─────────────────────────────────────

    @Test
    fun idleCycleOnlyReadsTheChangeMarker() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.tasks.createTask(phone.newTask("t1", "Buy milk"))

        phone.sync() // first cycle: full pull, then push + marker bump
        assertEquals(1, cloud.revisionWrites)
        phone.sync() // the marker moved (our own push) -> one more pull
        val listsWhenSettled = cloud.listCalls

        repeat(3) { phone.sync() } // nothing changed anywhere
        assertEquals(listsWhenSettled, cloud.listCalls, "idle cycles must not download collections")
        assertEquals(1, cloud.revisionWrites, "idle cycles must not rewrite the marker")
    }

    @Test
    fun changeFromAnotherDeviceArrivesOnTheNextCycle() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        val laptop = Device(cloud, "alice", clock)
        phone.sync(); phone.sync()

        clock.advance()
        laptop.tasks.createTask(laptop.newTask("t1", "From the laptop"))
        laptop.sync()

        phone.sync()
        assertEquals("From the laptop", phone.task("t1")?.title)
    }

    @Test
    fun safetyNetPullsChangesFromClientsThatDoNotBumpTheMarker() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.sync(); phone.sync()

        // An old app version writes a record without touching the marker.
        cloud.put("alice", TASKS, "old", taskFields("old", "From an old app"), updatedAt = clock.now, bumpRevision = false)
        phone.sync()
        assertNull(phone.task("old"), "not pulled yet: the marker did not move")

        clock.advance(SyncEngine.FULL_PULL_INTERVAL_MS)
        phone.sync()
        assertEquals("From an old app", phone.task("old")?.title)
    }

    @Test
    fun syncNowAlwaysDownloads() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.sync(); phone.sync()
        cloud.put("alice", TASKS, "x", taskFields("x", "Quiet change"), updatedAt = clock.now, bumpRevision = false)

        phone.sync(force = true)
        assertEquals("Quiet change", phone.task("x")?.title)
    }

    @Test
    fun aPulledCollectionIsAppliedWithLastWriteWins() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.tasks.createTask(phone.newTask("t1", "Local and newer"))
        cloud.put("alice", TASKS, "t1", taskFields("t1", "Remote but older"), updatedAt = clock.now - 60_000)
        cloud.put("alice", TASKS, "t2", taskFields("t2", "Remote only"), updatedAt = clock.now - 60_000)

        phone.sync()
        assertEquals("Local and newer", phone.task("t1")?.title)
        assertEquals("Remote only", phone.task("t2")?.title)
        assertEquals("Local and newer", cloud.doc("alice", TASKS, "t1")?.str("title"))
    }

    // ── #5: switching accounts ────────────────────────────────────────────────

    @Test
    fun switchingAccountsMergesLocalDataAtLowestPriorityAndLeavesTheOldAccountAlone() = runBlocking {
        val device = Device(cloud, "alice", clock)
        device.tasks.createTask(device.newTask("shared", "Alice's version"))
        device.tasks.createTask(device.newTask("mine", "Only Alice had this"))
        device.tasks.createTask(device.newTask("gone", "Alice deletes this"))
        device.sync()
        clock.advance()
        device.tasks.deleteTask("gone")
        device.sync()
        val aliceBefore = cloud.snapshot("alice")

        // Bob's account already has records with two of the same ids — older than Alice's edits.
        cloud.put("bob", TASKS, "shared", taskFields("shared", "Bob's version"), updatedAt = clock.now - 3_600_000)
        cloud.put("bob", TASKS, "gone", taskFields("gone", "Bob still has this"), updatedAt = clock.now - 3_600_000)

        device.auth.currentUid = "bob"
        clock.advance()
        device.sync()

        // Bob's own copy wins on this device, even though Alice's was edited later.
        assertEquals("Bob's version", device.task("shared")?.title)
        assertEquals("Bob's version", cloud.doc("bob", TASKS, "shared")?.str("title"))
        // Alice's other data is merged into Bob's account at the lowest priority.
        assertEquals(SyncEngine.LOWEST_PRIORITY_UPDATED_AT, cloud.doc("bob", TASKS, "mine")?.long(FS_META_UPDATED_AT))
        // Alice's delete must not delete Bob's record.
        assertFalse(cloud.doc("bob", TASKS, "gone")!!.bool(FS_META_DELETED))
        assertEquals("Bob still has this", device.task("gone")?.title)
        // Alice's account is untouched.
        assertEquals(aliceBefore, cloud.snapshot("alice"))
    }

    @Test
    fun signingBackIntoTheSameAccountKeepsPendingEditsAndRemoteChanges() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        val laptop = Device(cloud, "alice", clock)
        phone.tasks.createTask(phone.newTask("t1", "v1"))
        phone.sync(); laptop.sync()

        phone.auth.currentUid = null // signed out, keeps working offline
        clock.advance()
        phone.tasks.createTask(phone.newTask("offline", "Made while signed out"))
        clock.advance()
        laptop.tasks.updateTask(laptop.task("t1")!!.copy(title = "v2 from laptop"))
        laptop.sync()

        phone.auth.currentUid = "alice"
        phone.sync()
        assertEquals("v2 from laptop", phone.task("t1")?.title, "a newer edit from another device must not be overwritten")
        assertNotNull(cloud.doc("alice", TASKS, "offline"), "edits made while signed out are uploaded")
        assertEquals("v2 from laptop", cloud.doc("alice", TASKS, "t1")?.str("title"))
    }

    // ── #4: built-in content (sample tasks, default categories) ──────────────

    @Test
    fun sampleTasksStayLocalUntilEdited() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.categories.initDefaultCategories()
        phone.tasks.addSampleTasks(SampleTasks.build(Instant.fromEpochMilliseconds(clock.now)))
        phone.sync()
        assertTrue(cloud.ids("alice", TASKS).isEmpty(), "untouched samples are not uploaded")
        assertTrue(cloud.ids("alice", CATEGORIES).isEmpty(), "untouched default categories are not uploaded")

        clock.advance()
        phone.tasks.toggleTaskCompletion("sample-task-2", true)
        phone.sync()
        assertEquals(setOf("sample-task-2"), cloud.ids("alice", TASKS))
    }

    @Test
    fun secondDeviceDoesNotDuplicateSampleTasksAndGetsTheEditedOne() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.tasks.addSampleTasks(SampleTasks.build(Instant.fromEpochMilliseconds(clock.now)))
        clock.advance()
        phone.tasks.toggleTaskCompletion("sample-task-2", true)
        phone.sync()

        clock.advance()
        val laptop = Device(cloud, "alice", clock) // fresh install, same account
        laptop.tasks.addSampleTasks(SampleTasks.build(Instant.fromEpochMilliseconds(clock.now)))
        laptop.sync()

        assertEquals(7, laptop.taskIds().size, "still seven samples, not fourteen")
        assertEquals(true, laptop.task("sample-task-2")?.isCompleted)
        assertEquals(setOf("sample-task-2"), cloud.ids("alice", TASKS))
    }

    @Test
    fun freshInstallCannotOverwriteARenamedDefaultCategory() = runBlocking {
        val phone = Device(cloud, "alice", clock)
        phone.categories.initDefaultCategories()
        val food = phone.categories.getAllCategories().first().first { it.id == "exp_food" }
        clock.advance()
        phone.categories.updateCategory(food.copy(name = "Groceries"))
        phone.sync()

        clock.advance()
        val laptop = Device(cloud, "alice", clock) // fresh install creates its own "Food"
        laptop.categories.initDefaultCategories()
        laptop.sync()

        assertEquals("Groceries", laptop.categoryName("exp_food"))
        assertEquals("Groceries", cloud.doc("alice", CATEGORIES, "exp_food")?.str("name"))
    }
}
