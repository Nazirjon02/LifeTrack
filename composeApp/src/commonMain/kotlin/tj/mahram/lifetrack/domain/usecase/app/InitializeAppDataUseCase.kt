package tj.mahram.lifetrack.domain.usecase.app

import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Instant
import tj.mahram.lifetrack.domain.model.SampleTasks
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TaskRepository

/**
 * Runs on every app start: makes sure the default categories exist and, on the
 * very first launch of a fresh install only, adds the sample tasks.
 *
 * Sample tasks used to be re-created whenever the task list was empty, so they
 * came back every time the user deleted them all (and spread to other devices
 * through sync).
 */
class InitializeAppDataUseCase(
    private val settings: SettingsRepository,
    private val categories: CategoryRepository,
    private val tasks: TaskRepository
) {
    suspend operator fun invoke(now: Instant = Clock.System.now()) {
        val firstLaunch = settings.isFirstLaunch()
        // Installs that were used before the first-launch flag existed already
        // have categories — those users must not get the samples again.
        val freshInstall = firstLaunch && categories.getAllCategories().first().isEmpty()
        categories.initDefaultCategories()
        if (freshInstall && tasks.getAllTasks().first().isEmpty()) {
            tasks.addSampleTasks(SampleTasks.build(now))
        }
        if (firstLaunch) settings.setFirstLaunchDone()
    }
}
