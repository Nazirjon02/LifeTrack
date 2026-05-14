package tj.mahram.lifetrack.di

import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import tj.mahram.lifetrack.data.local.CategoryLocalDataSource
import tj.mahram.lifetrack.data.local.CryptoLocalDataSource
import tj.mahram.lifetrack.data.local.DatabaseDriverFactory
import tj.mahram.lifetrack.data.local.TaskLocalDataSource
import tj.mahram.lifetrack.data.local.TransactionLocalDataSource
import tj.mahram.lifetrack.data.local.db.AppDatabase
import tj.mahram.lifetrack.data.remote.CoinGeckoApi
import tj.mahram.lifetrack.data.remote.createHttpClient
import tj.mahram.lifetrack.data.repository.CategoryRepositoryImpl
import tj.mahram.lifetrack.data.repository.CryptoRepositoryImpl
import tj.mahram.lifetrack.data.repository.SettingsRepositoryImpl
import tj.mahram.lifetrack.data.repository.TaskRepositoryImpl
import tj.mahram.lifetrack.data.repository.TransactionRepositoryImpl
import tj.mahram.lifetrack.domain.repository.CategoryRepository
import tj.mahram.lifetrack.domain.repository.CryptoRepository
import tj.mahram.lifetrack.domain.repository.SettingsRepository
import tj.mahram.lifetrack.domain.repository.TaskRepository
import tj.mahram.lifetrack.domain.repository.TransactionRepository
import tj.mahram.lifetrack.domain.usecase.crypto.AddPriceAlertUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.AddToPortfolioUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetCoinDetailUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetCoinPriceHistoryUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetPortfolioItemsUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetPortfolioSummaryUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetPriceAlertsUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetTopCoinsUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.GetWatchlistUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.RemoveFromPortfolioUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.SearchCoinsUseCase
import tj.mahram.lifetrack.domain.usecase.crypto.ToggleWatchlistUseCase
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
import tj.mahram.lifetrack.feature.crypto.CryptoScreenModel
import tj.mahram.lifetrack.feature.dashboard.DashboardScreenModel
import tj.mahram.lifetrack.feature.finance.FinanceScreenModel
import tj.mahram.lifetrack.feature.planner.PlannerScreenModel
import tj.mahram.lifetrack.feature.settings.SettingsScreenModel

val appModule = module {
    // Network
    single { createHttpClient() }
    singleOf(::CoinGeckoApi)

    // Database
    single { get<DatabaseDriverFactory>().create() }
    single { AppDatabase(get()) }

    // Settings
    single { Settings() }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Local data sources
    singleOf(::TaskLocalDataSource)
    singleOf(::TransactionLocalDataSource)
    singleOf(::CryptoLocalDataSource)
    singleOf(::CategoryLocalDataSource)

    // Repositories
    single<TaskRepository> { TaskRepositoryImpl(get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CryptoRepository> { CryptoRepositoryImpl(get(), get()) }
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

    // Crypto use cases
    factoryOf(::GetTopCoinsUseCase)
    factoryOf(::SearchCoinsUseCase)
    factoryOf(::GetCoinDetailUseCase)
    factoryOf(::GetCoinPriceHistoryUseCase)
    factoryOf(::GetPortfolioItemsUseCase)
    factoryOf(::AddToPortfolioUseCase)
    factoryOf(::RemoveFromPortfolioUseCase)
    factoryOf(::GetWatchlistUseCase)
    factoryOf(::ToggleWatchlistUseCase)
    factoryOf(::GetPortfolioSummaryUseCase)
    factoryOf(::AddPriceAlertUseCase)
    factoryOf(::GetPriceAlertsUseCase)

    // Screen models
    factoryOf(::DashboardScreenModel)
    factoryOf(::PlannerScreenModel)
    factoryOf(::FinanceScreenModel)
    factoryOf(::CryptoScreenModel)
    factoryOf(::SettingsScreenModel)
}
