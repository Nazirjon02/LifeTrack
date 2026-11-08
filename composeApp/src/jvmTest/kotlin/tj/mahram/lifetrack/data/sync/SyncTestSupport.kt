package tj.mahram.lifetrack.data.sync

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import tj.mahram.lifetrack.data.local.CategoryLocalDataSource
import tj.mahram.lifetrack.data.local.TaskLocalDataSource
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.data.remote.firebase.FS_META_DELETED
import tj.mahram.lifetrack.data.remote.firebase.FS_META_UPDATED_AT
import tj.mahram.lifetrack.data.remote.firebase.FsFields
import tj.mahram.lifetrack.data.remote.firebase.FsValue
import tj.mahram.lifetrack.data.remote.firebase.RemoteDoc
import tj.mahram.lifetrack.data.remote.firebase.RemoteStore
import tj.mahram.lifetrack.data.repository.CategoryRepositoryImpl
import tj.mahram.lifetrack.data.repository.TaskRepositoryImpl
import tj.mahram.lifetrack.domain.model.AuthState
import tj.mahram.lifetrack.domain.model.Task
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.domain.repository.AuthRepository

class TestClock(var now: Long = 1_700_000_000_000L) {
    fun advance(ms: Long = 1_000L) { now += ms }
}

/** In-memory stand-in for the Realtime Database, shared by several test "devices". */
class FakeCloud : RemoteStore {
    /** uid -> collection -> id -> stored fields (entity columns + _updatedAt/_deleted). */
    val records = mutableMapOf<String, MutableMap<String, MutableMap<String, Map<String, FsValue>>>>()
    val revisions = mutableMapOf<String, String>()
    var listCalls = 0
    var revisionWrites = 0

    override suspend fun upsert(uid: String, collection: String, docId: String, fields: Map<String, FsValue>, idToken: String) {
        bucket(uid, collection)[docId] = fields
    }

    override suspend fun list(uid: String, collection: String, idToken: String): List<RemoteDoc> {
        listCalls++
        return records[uid]?.get(collection).orEmpty().map { (id, fields) ->
            val f = FsFields(fields)
            RemoteDoc(id, f, f.long(FS_META_UPDATED_AT) ?: 0L, f.bool(FS_META_DELETED))
        }
    }

    override suspend fun readRevision(uid: String, idToken: String): String? = revisions[uid]

    override suspend fun writeRevision(uid: String, revision: String, idToken: String) {
        revisionWrites++
        revisions[uid] = revision
    }

    fun doc(uid: String, collection: String, id: String): FsFields? =
        records[uid]?.get(collection)?.get(id)?.let { FsFields(it) }

    fun ids(uid: String, collection: String): Set<String> = records[uid]?.get(collection)?.keys.orEmpty().toSet()

    /** A record written by some other client; [bumpRevision] = false mimics an old app version. */
    fun put(uid: String, collection: String, id: String, fields: Map<String, FsValue>, updatedAt: Long, bumpRevision: Boolean = true) {
        bucket(uid, collection)[id] = fields + mapOf(
            FS_META_UPDATED_AT to FsValue.Int64(updatedAt),
            FS_META_DELETED to FsValue.Bool(false)
        )
        if (bumpRevision) revisions[uid] = "other-$updatedAt"
    }

    fun snapshot(uid: String): Map<String, Map<String, Map<String, FsValue>>> =
        records[uid].orEmpty().mapValues { (_, docs) -> docs.toMap() }

    private fun bucket(uid: String, collection: String) =
        records.getOrPut(uid) { mutableMapOf() }.getOrPut(collection) { mutableMapOf() }
}

class FakeAuth(var currentUid: String?) : AuthRepository {
    /** Thrown by [validIdToken], like a token refresh with no connection. */
    var tokenFailure: Throwable? = null
    /** The server rejected the refresh token: [validIdToken] returns null. */
    var sessionRejected = false

    override val state: StateFlow<AuthState> = MutableStateFlow(AuthState.SignedOut)
    override suspend fun signIn(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun signOut() { currentUid = null }
    override fun uid(): String? = currentUid
    override suspend fun validIdToken(): String? {
        tokenFailure?.let { throw it }
        if (sessionRejected) return null
        return currentUid?.let { "token-$it" }
    }
}

/** One device: its own database, the real repositories and the real sync engine. */
class Device(cloud: FakeCloud, uid: String?, private val clock: TestClock) {
    val db = AppDatabase(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { AppDatabase.Schema.create(it) })
    val auth = FakeAuth(uid)
    val tracker = SyncTracker(db) { clock.now }
    val tasks = TaskRepositoryImpl(TaskLocalDataSource(db), tracker)
    val categories = CategoryRepositoryImpl(CategoryLocalDataSource(db), tracker)
    val engine = SyncEngine(auth, cloud, tracker, defaultSyncCollections(db) { clock.now }, { clock.now })

    suspend fun sync(force: Boolean = false) {
        engine.syncAll(force).getOrThrow()
    }

    suspend fun task(id: String): Task? = tasks.getTaskById(id)

    suspend fun taskIds(): Set<String> = tasks.getAllTasks().first().map { it.id }.toSet()

    suspend fun categoryName(id: String): String? = categories.getAllCategories().first().firstOrNull { it.id == id }?.name

    fun newTask(id: String, title: String): Task = Task(
        id = id,
        title = title,
        description = null,
        priority = TaskPriority.MEDIUM,
        categoryId = null,
        isCompleted = false,
        dueDate = null,
        createdAt = Instant.fromEpochMilliseconds(clock.now),
        updatedAt = Instant.fromEpochMilliseconds(clock.now),
        isRecurring = false,
        recurringType = null,
        parentTaskId = null
    )
}

fun taskFields(id: String, title: String, completed: Boolean = false): Map<String, FsValue> = mapOf(
    "id" to FsValue.Str(id),
    "title" to FsValue.Str(title),
    "isCompleted" to FsValue.Int64(if (completed) 1L else 0L)
)
