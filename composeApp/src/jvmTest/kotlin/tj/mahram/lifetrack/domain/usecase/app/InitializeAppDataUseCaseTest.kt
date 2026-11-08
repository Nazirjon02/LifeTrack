package tj.mahram.lifetrack.domain.usecase.app

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import tj.mahram.lifetrack.data.sync.Device
import tj.mahram.lifetrack.data.sync.FakeCloud
import tj.mahram.lifetrack.data.sync.TestClock
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.AppSettings
import tj.mahram.lifetrack.domain.model.AppTheme
import tj.mahram.lifetrack.domain.model.Category
import tj.mahram.lifetrack.domain.model.CategoryType
import tj.mahram.lifetrack.domain.repository.SettingsRepository

class InitializeAppDataUseCaseTest {
    private val device = Device(FakeCloud(), uid = null, clock = TestClock())

    @Test
    fun freshInstallGetsTheSampleTasksExactlyOnce() = runBlocking {
        val settings = FakeSettings(firstLaunch = true)
        val init = InitializeAppDataUseCase(settings, device.categories, device.tasks)

        init()
        assertEquals(7, device.taskIds().size)
        assertFalse(settings.firstLaunch)

        // The user deletes every task and restarts the app: they must not come back.
        device.taskIds().forEach { device.tasks.deleteTask(it) }
        init()
        assertTrue(device.taskIds().isEmpty())
    }

    @Test
    fun installFromBeforeTheFlagExistedDoesNotGetSamplesAgain() = runBlocking {
        // Older versions never wrote the first-launch flag, but they did create categories.
        device.categories.createCategory(Category("c1", "Work", CategoryType.TASK, "💼", "#4F46E5", false))
        val settings = FakeSettings(firstLaunch = true)

        InitializeAppDataUseCase(settings, device.categories, device.tasks)()

        assertTrue(device.taskIds().isEmpty())
        assertFalse(settings.firstLaunch)
    }

    @Test
    fun defaultCategoriesAreCreatedOnEveryLaunchIfMissing() = runBlocking {
        val init = InitializeAppDataUseCase(FakeSettings(firstLaunch = false), device.categories, device.tasks)
        init()
        assertTrue(device.categories.getAllCategories().first().isNotEmpty())
        assertTrue(device.taskIds().isEmpty(), "no samples when it's not the first launch")
    }
}

private class FakeSettings(var firstLaunch: Boolean) : SettingsRepository {
    override fun getSettings(): Flow<AppSettings> = error("not used")
    override suspend fun setTheme(theme: AppTheme) = Unit
    override suspend fun setCurrency(currency: String) = Unit
    override suspend fun setOpeningBalance(amount: Double) = Unit
    override suspend fun setLanguage(language: AppLanguage) = Unit
    override suspend fun setNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setTaskNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setFinanceNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setCryptoNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun setDisplayName(name: String) = Unit
    override suspend fun setAvatarEmoji(emoji: String) = Unit
    override suspend fun isFirstLaunch(): Boolean = firstLaunch
    override suspend fun setFirstLaunchDone() { firstLaunch = false }
}
