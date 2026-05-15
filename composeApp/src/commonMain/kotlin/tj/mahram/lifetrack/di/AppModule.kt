package tj.mahram.lifetrack.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import tj.mahram.lifetrack.data.local.CategoryLocalDataSource
import tj.mahram.lifetrack.data.local.DatabaseDriverFactory
import tj.mahram.lifetrack.data.local.TaskLocalDataSource
import tj.mahram.lifetrack.data.local.TransactionLocalDataSource
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.data.repository.CategoryRepositoryImpl
import tj.mahram.lifetrack.data.repository.SettingsRepositoryImpl
import tj.mahram.lifetrack.data.repository.TaskRepositoryImpl
import tj.mahram.lifetrack.data.repository.TransactionRepositoryImpl
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TaskRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository
import tj.mahram.lifetrack.domain.usecase.finance.AddTransactionUseCase
import tj.mahram.lifetrack.domain.usecase.finance.DeleteTransactionUseCase
import tj.mahram.lifetrack.domain.usecase.finance.GetAllTransactionsUseCase
import tj.mahram.lifetrack.domain.usecase.finance.GetFinanceSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.finance.GetRecentTransactionsUseCase
import tj.mahram.lifetrack.domain.usecase.finance.GetTransactionsByDateRangeUseCase
import tj.mahram.lifetrack.domain.usecase.task.CreateTaskUseCase
import tj.mahram.lifetrack.domain.usecase.task.DeleteTaskUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetAllTasksUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTaskStatsUseCase
import tj.mahram.lifetrack.domain.usecase.task.GetTodayTasksUseCase
import tj.mahram.lifetrack.domain.usecase.task.SearchTasksUseCase
import tj.mahram.lifetrack.domain.usecase.task.ToggleTaskCompletionUseCase
import tj.mahram.lifetrack.domain.usecase.task.UpdateTaskUseCase
import tj.mahram.lifetrack.feature.dashboard.DashboardScreenModel
import tj.mahram.lifetrack.feature.finance.FinanceScreenModel
import tj.mahram.lifetrack.feature.planner.PlannerScreenModel
import tj.mahram.lifetrack.feature.settings.SettingsScreenModel

val appModule = module {
    // Database
    single { get<DatabaseDriverFactory>().create() }
    single { AppDatabase(get()) }

    // Settings
    single { Settings() }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Local data sources
    singleOf(::TaskLocalDataSource)
    singleOf(::TransactionLocalDataSource)
    singleOf(::CategoryLocalDataSource)

    // Repositories
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }

    // Task use cases
    factoryOf(::GetAllTasksUseCase)
    factoryOf(::GetTodayTasksUseCase)
    factoryOf(::SearchTasksUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::UpdateTaskUseCase)
    factoryOf(::ToggleTaskCompletionUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::GetTaskStatsUseCase)

    // Finance use cases
    factoryOf(::GetAllTransactionsUseCase)
    factoryOf(::GetTransactionsByDateRangeUseCase)
    factoryOf(::GetRecentTransactionsUseCase)
    factoryOf(::AddTransactionUseCase)
    factoryOf(::DeleteTransactionUseCase)
    factoryOf(::GetFinanceSummaryUseCase)

    // Screen models
    factoryOf(::DashboardScreenModel)
    factoryOf(::PlannerScreenModel)
    factoryOf(::FinanceScreenModel)
    factoryOf(::SettingsScreenModel)
}
