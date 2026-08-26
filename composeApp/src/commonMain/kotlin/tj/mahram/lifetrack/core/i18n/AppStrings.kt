package tj.mahram.lifetrack.core.i18n

import androidx.compose.runtime.compositionLocalOf
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.HabitFrequency
import tj.mahram.lifetrack.domain.model.TaskPriority
import tj.mahram.lifetrack.domain.model.TransactionType

data class AppStrings(
    // Navigation tabs
    val tabDashboard: String,
    val tabPlanner: String,
    val tabFinance: String,
    val tabSettings: String,

    // Settings screen
    val settingsTitle: String,
    val settingsSubtitle: String,
    val sectionAppearance: String,
    val labelTheme: String,
    val themeDark: String,
    val themeLight: String,
    val themeSystem: String,
    val sectionLanguage: String,
    val labelAppLanguage: String,
    val sectionFinance: String,
    val labelBaseCurrency: String,
    val labelCurrencySubtitle: String,
    val sectionNotifications: String,
    val labelEnableNotif: String,
    val labelEnableNotifSub: String,
    val labelTaskReminders: String,
    val labelTaskRemindersSub: String,
    val labelFinanceAlerts: String,
    val labelFinanceAlertsSub: String,
    val sectionAbout: String,
    val aboutVersion: String,
    val settingsAppTagline: String,
    val settingsPremium: String,
    val settingsVersionLabel: String,

    // Profile screen
    val tabProfile: String,
    val profileEditTitle: String,
    val profileNameLabel: String,
    val profileNameHint: String,
    val profileDefaultName: String,
    val profilePickAvatar: String,
    val profileLevelLabel: (Int) -> String,
    val profileScoreLabel: String,
    val profileToNextLevel: (Int) -> String,
    val profileStatBalance: String,
    val profileStatTasks: String,
    val profileStatHabits: String,
    val profileStatGoals: String,
    val profileStatStreak: String,
    val profileEditAction: String,
    val profileSettingsHeader: String,
    val profileRankTitles: List<String>,

    // Planner screen — header
    val plannerProgressReady: String,
    val plannerProgressDone: String,
    val plannerProgressAlmostThere: String,
    val plannerProgressHalfway: String,
    val plannerProgressKeepGoing: String,
    val plannerProgressLetsGo: String,
    val plannerAllTasksDone: String,
    val plannerAddFirstTask: String,
    val plannerTasksRemaining: (Int) -> String,
    val plannerDoneLabel: String,
    val plannerLabelTotal: String,
    val plannerLabelDone: String,
    val plannerLabelLeft: String,
    val plannerPriorityBreakdown: String,

    // Planner screen — general
    val plannerTabPending: String,
    val plannerTabDone: String,
    val plannerFabNewTask: String,
    val plannerSearchHint: String,
    val plannerEmptyPendingTitle: String,
    val plannerEmptyPendingSubtitle: String,
    val plannerEmptyDoneTitle: String,
    val plannerEmptyDoneSubtitle: String,
    val plannerAddTaskAction: String,

    // AddTask sheet
    val addTaskTitle: String,
    val addTaskSubtitle: String,
    val addTaskFieldTitle: String,
    val addTaskFieldNote: String,
    val addTaskPriority: String,
    val addTaskDueDate: String,
    val addTaskDueNone: String,
    val addTaskDueToday: String,
    val addTaskDueTomorrow: String,
    val addTaskDueWeek: String,
    val addTaskCategory: String,
    val addTaskCategoryNone: String,
    val addTaskButton: String,
    val addTaskErrorEmpty: String,

    // Priority labels
    val priorityLow: String,
    val priorityMedium: String,
    val priorityHigh: String,
    val priorityCritical: String,

    // Dashboard
    val dashboardGreetingMorning: String,
    val dashboardGreetingAfternoon: String,
    val dashboardGreetingEvening: String,
    val dashboardGreetingNight: String,
    val dashboardTasksToday: String,
    val dashboardNoTasks: String,
    val dashboardFinanceMonth: String,
    val dashboardIncome: String,
    val dashboardExpense: String,
    val dashboardBalance: String,
    val dashboardHabitsDoneLabel: String,
    val dashboardTodaysHabits: String,
    val dashboardHabitsDoneCount: (done: Int, total: Int) -> String,

    // Habits
    val habitsTitle: String,
    val habitsSubtitle: (done: Int, total: Int) -> String,
    val habitsCompletionPerfect: String,
    val habitsCompletionGreat: String,
    val habitsCompletionKeepGoing: String,
    val habitsCompletionStart: String,
    val habitsBestStreak: (days: Int) -> String,
    val habitsDayStreak: (days: Int) -> String,
    val habitsEmptyTitle: String,
    val habitsEmptySubtitle: String,
    val habitFreqDaily: String,
    val habitFreqWeekdays: String,
    val habitFreqWeekends: String,

    // AddHabit
    val addHabitTitle: String,
    val addHabitFieldName: String,
    val addHabitErrorEmpty: String,
    val addHabitPickIcon: String,
    val addHabitColorLabel: String,
    val addHabitFrequencyLabel: String,
    val addHabitCreateButton: String,

    // Goals
    val goalsTitle: String,
    val goalsSubtitle: (completed: Int, total: Int) -> String,
    val goalsActiveSection: String,
    val goalsCompletedSection: String,
    val goalsCompletionAll: String,
    val goalsCompletionHalfway: String,
    val goalsCompletionKeepPushing: String,
    val goalsCompletionSet: String,
    val goalsAchieved: (completed: Int, total: Int) -> String,
    val goalsActiveCount: (active: Int) -> String,
    val goalsUpdateProgress: String,
    val addGoalSheetTitle: String,
    val addGoalSheetSubtitle: String,
    val addGoalFieldTitle: String,
    val addGoalPlaceholderTitle: String,
    val addGoalErrorTitleEmpty: String,
    val addGoalFieldTarget: String,
    val addGoalFieldUnit: String,
    val addGoalCreateButton: String,
    val updateGoalProgressLabel: String,
    val updateGoalCurrentProgress: (current: String, target: String, unit: String) -> String,
    val updateGoalButton: String,
    val cancelButton: String,
    val goalsEmptyTitle: String,
    val goalsEmptySubtitle: String,

    // Finance
    val financeTitle: String,
    val financeSubtitle: String,
    val financeTotalBalance: String,
    val financeIncomeLabel: String,
    val financeExpensesLabel: String,
    val financeTabAll: String,
    val financeTabExpenses: String,
    val financeTabIncome: String,
    val financeExpenseBreakdown: String,
    val financeTotalLabel: String,
    val financeExpenseTrend: String,
    val financeRecords: (count: Int) -> String,
    val financeEmptyTitle: String,
    val financeEmptySubtitle: String,
    val financeAmountLabel: String,
    val financeAmountError: String,
    val financeCategoryLabel: String,
    val financeNoteLabel: String,
    val financeAddTitleIncome: String,
    val financeAddTitleExpense: String,

    // Central balance
    val financeAdjustTitle: String,
    val financeAdjustHint: String,
    val saveButton: String,

    // Purchase goals (goals linked to the central balance)
    val goalAffectsBalanceBadge: String,
    val goalPurchasedBadge: String,
    val goalUndoPurchase: String,
    val goalMarkPurchased: String,
    val goalPurchaseToggle: String,
    val goalPurchaseToggleHint: String,
    val goalPriceLabel: String,

    // Planner screen title & view switcher
    val plannerScreenTitle: String,
    val plannerViewYear: String,
    val plannerViewMonth: String,
    val plannerViewDay: String,

    // Planner calendar cells & day view
    val plannerNoTasksMonth: String,
    val plannerTasksCountMonth: (Int) -> String,
    val plannerTasksDoneOf: (Int, Int) -> String,
    val plannerEmptyDayTitle: String,
    val plannerEmptyDaySubtitle: String,
    val plannerDoneTabSubtitle: String,
    val plannerMonthAbbrevs: List<String>,
    val plannerMonthFullNames: List<String>,
    val plannerWeekDayHeaders: List<String>,
    val plannerDayOfWeekNames: List<String>,

    // Crypto
    val cryptoTitle: String,
    val cryptoSearchPlaceholder: String,
    val cryptoMarketEmpty: String,
    val cryptoMarketEmptySubtitle: String,
    val cryptoPortfolioEmpty: String,
    val cryptoPortfolioEmptySubtitle: String,
    val cryptoWatchlistEmpty: String,
    val cryptoWatchlistEmptySubtitle: String,
    val cryptoRemove: String,
    val cryptoChart1D: String,
    val cryptoChart7D: String,
    val cryptoChart1M: String,
    val cryptoChart1Y: String,
    val cryptoAddToPortfolioButton: String,
    val cryptoWatchlistButton: String,
    val cryptoAddToPortfolioDialog: (coinName: String) -> String,
    val cryptoAmountLabel: String,
    val cryptoPurchasePriceLabel: String,
    val cryptoAddButton: String,
    val cryptoCancelButton: String,
) {
    fun priorityLabel(priority: TaskPriority): String = when (priority) {
        TaskPriority.LOW      -> priorityLow
        TaskPriority.MEDIUM   -> priorityMedium
        TaskPriority.HIGH     -> priorityHigh
        TaskPriority.CRITICAL -> priorityCritical
    }

    fun habitFrequencyLabel(frequency: HabitFrequency): String = when (frequency) {
        HabitFrequency.DAILY    -> habitFreqDaily
        HabitFrequency.WEEKDAYS -> habitFreqWeekdays
        HabitFrequency.WEEKENDS -> habitFreqWeekends
        HabitFrequency.CUSTOM   -> habitFreqDaily
    }

    fun transactionTypeLabel(type: TransactionType): String = when (type) {
        TransactionType.INCOME  -> financeAddTitleIncome
        TransactionType.EXPENSE -> financeAddTitleExpense
    }
}

val EnglishStrings = AppStrings(
    tabDashboard = "Dashboard",
    tabPlanner = "Planner",
    tabFinance = "Finance",
    tabSettings = "Settings",

    settingsTitle = "Settings",
    settingsSubtitle = "Customize your experience",
    sectionAppearance = "🎨  Appearance",
    labelTheme = "Theme",
    themeDark = "Dark",
    themeLight = "Light",
    themeSystem = "System",
    sectionLanguage = "🌐  Language",
    labelAppLanguage = "App Language",
    sectionFinance = "💰  Finance",
    labelBaseCurrency = "Base Currency",
    labelCurrencySubtitle = "Used across finance features",
    sectionNotifications = "🔔  Notifications",
    labelEnableNotif = "Enable Notifications",
    labelEnableNotifSub = "Master switch for all alerts",
    labelTaskReminders = "Task Reminders",
    labelTaskRemindersSub = "Due date and deadline alerts",
    labelFinanceAlerts = "Finance Alerts",
    labelFinanceAlertsSub = "Budget and spending notifications",
    sectionAbout = "ℹ️  About",
    aboutVersion = "Version 1.0.0 · Kotlin Multiplatform",
    settingsAppTagline = "Your personal life manager",
    settingsPremium = "✨ Premium",
    settingsVersionLabel = "v1.0",

    tabProfile = "Profile",
    profileEditTitle = "Edit profile",
    profileNameLabel = "Your name",
    profileNameHint = "Enter your name",
    profileDefaultName = "LifeTracker",
    profilePickAvatar = "Choose your avatar",
    profileLevelLabel = { n -> "Level $n" },
    profileScoreLabel = "Productivity score",
    profileToNextLevel = { n -> "$n pts to next level" },
    profileStatBalance = "Balance",
    profileStatTasks = "Tasks done",
    profileStatHabits = "Habits",
    profileStatGoals = "Goals hit",
    profileStatStreak = "Best streak",
    profileEditAction = "Edit profile",
    profileSettingsHeader = "Settings",
    profileRankTitles = listOf("Beginner", "Explorer", "Achiever", "Pro", "Champion", "Master", "Legend"),

    plannerProgressReady = "Ready to start! 🚀",
    plannerProgressDone = "All done! 🎉",
    plannerProgressAlmostThere = "Almost there! 💪",
    plannerProgressHalfway = "Halfway through! ✨",
    plannerProgressKeepGoing = "Keep going! 🔥",
    plannerProgressLetsGo = "Let's get started! ✨",
    plannerAllTasksDone = "All tasks completed!",
    plannerAddFirstTask = "Add your first task below",
    plannerTasksRemaining = { n -> if (n == 1) "1 task remaining" else "$n tasks remaining" },
    plannerDoneLabel = "done",
    plannerLabelTotal = "Total",
    plannerLabelDone = "Done",
    plannerLabelLeft = "Left",
    plannerPriorityBreakdown = "By Priority",

    plannerTabPending = "Pending",
    plannerTabDone = "Done",
    plannerFabNewTask = "New Task",
    plannerSearchHint = "Search tasks…",
    plannerEmptyPendingTitle = "All done! 🎉",
    plannerEmptyPendingSubtitle = "Tap New Task to add something",
    plannerEmptyDoneTitle = "No completed tasks",
    plannerEmptyDoneSubtitle = "Complete tasks to see them here",
    plannerAddTaskAction = "Add Task",

    addTaskTitle = "New Task ✨",
    addTaskSubtitle = "What do you want to accomplish?",
    addTaskFieldTitle = "Task title",
    addTaskFieldNote = "Note (optional)",
    addTaskPriority = "Priority",
    addTaskDueDate = "Due Date",
    addTaskDueNone = "None",
    addTaskDueToday = "Today",
    addTaskDueTomorrow = "Tomorrow",
    addTaskDueWeek = "This week",
    addTaskCategory = "Category",
    addTaskCategoryNone = "None",
    addTaskButton = "Create Task",
    addTaskErrorEmpty = "Please enter a task title",

    priorityLow = "Low",
    priorityMedium = "Medium",
    priorityHigh = "High",
    priorityCritical = "Critical",

    dashboardGreetingMorning = "Good morning",
    dashboardGreetingAfternoon = "Good afternoon",
    dashboardGreetingEvening = "Good evening",
    dashboardGreetingNight = "Good night",
    dashboardTasksToday = "Tasks Today",
    dashboardNoTasks = "No tasks for today",
    dashboardFinanceMonth = "This Month",
    dashboardIncome = "Income",
    dashboardExpense = "Expenses",
    dashboardBalance = "Balance",
    dashboardHabitsDoneLabel = "Habits Done",
    dashboardTodaysHabits = "Today's Habits",
    dashboardHabitsDoneCount = { done, total -> "$done/$total done" },

    habitsTitle = "Habits",
    habitsSubtitle = { done, total -> "$done/$total done today" },
    habitsCompletionPerfect = "Perfect day! 🌟",
    habitsCompletionGreat = "Great progress! 🔥",
    habitsCompletionKeepGoing = "Keep going! 💪",
    habitsCompletionStart = "Start your day! ✨",
    habitsBestStreak = { n -> "🔥 Best streak: $n days" },
    habitsDayStreak = { n -> "🔥 $n day streak" },
    habitsEmptyTitle = "No habits yet",
    habitsEmptySubtitle = "Build positive routines one day at a time",
    habitFreqDaily = "Daily",
    habitFreqWeekdays = "Weekdays",
    habitFreqWeekends = "Weekends",

    addHabitTitle = "New Habit ✨",
    addHabitFieldName = "Habit name",
    addHabitErrorEmpty = "Please enter a name",
    addHabitPickIcon = "Pick an icon",
    addHabitColorLabel = "Color",
    addHabitFrequencyLabel = "Frequency",
    addHabitCreateButton = "Create Habit",

    goalsTitle = "Goals",
    goalsSubtitle = { completed, total -> "$completed/$total completed" },
    goalsActiveSection = "Active",
    goalsCompletedSection = "Completed",
    goalsCompletionAll = "All goals done! 🌟",
    goalsCompletionHalfway = "Halfway there! 🚀",
    goalsCompletionKeepPushing = "Keep pushing! 💪",
    goalsCompletionSet = "Set your goals! 🎯",
    goalsAchieved = { completed, total -> "$completed of $total goals achieved" },
    goalsActiveCount = { n -> "🔥 $n active" },
    goalsUpdateProgress = "＋ Update progress",
    addGoalSheetTitle = "🎯 New Goal",
    addGoalSheetSubtitle = "What do you want to achieve?",
    addGoalFieldTitle = "Goal title",
    addGoalPlaceholderTitle = "e.g. Run 100km",
    addGoalErrorTitleEmpty = "Title can't be empty",
    addGoalFieldTarget = "Target",
    addGoalFieldUnit = "Unit",
    addGoalCreateButton = "Create Goal →",
    updateGoalProgressLabel = "New progress value:",
    updateGoalCurrentProgress = { current, target, unit -> "Current: $current / $target $unit" },
    updateGoalButton = "Update",
    cancelButton = "Cancel",
    goalsEmptyTitle = "No goals yet",
    goalsEmptySubtitle = "Set a goal and track your progress",

    financeTitle = "Finance",
    financeSubtitle = "Track your money",
    financeTotalBalance = "Total Balance",
    financeIncomeLabel = "Income",
    financeExpensesLabel = "Expenses",
    financeTabAll = "All",
    financeTabExpenses = "Expenses",
    financeTabIncome = "Income",
    financeExpenseBreakdown = "Expense Breakdown",
    financeTotalLabel = "total",
    financeExpenseTrend = "Expense Trend",
    financeRecords = { n -> "$n records" },
    financeEmptyTitle = "No transactions",
    financeEmptySubtitle = "Tap + to add your first transaction",
    financeAmountLabel = "Amount *",
    financeAmountError = "Enter a valid amount",
    financeCategoryLabel = "Category *",
    financeNoteLabel = "Note (optional)",
    financeAddTitleIncome = "Income",
    financeAddTitleExpense = "Expense",

    financeAdjustTitle = "Set your balance",
    financeAdjustHint = "Enter how much money you have right now — it stays in sync with your income and expenses.",
    saveButton = "Save",

    goalAffectsBalanceBadge = "Affects balance",
    goalPurchasedBadge = "Purchased",
    goalUndoPurchase = "Undo",
    goalMarkPurchased = "Mark as bought",
    goalPurchaseToggle = "Purchase goal",
    goalPurchaseToggleHint = "Deducts the price from your balance when bought",
    goalPriceLabel = "Price",

    plannerScreenTitle = "Planner",
    plannerViewYear = "Year",
    plannerViewMonth = "Month",
    plannerViewDay = "Day",

    plannerNoTasksMonth = "No tasks",
    plannerTasksCountMonth = { n -> if (n == 1) "1 task" else "$n tasks" },
    plannerTasksDoneOf = { done, total -> "$done of $total tasks done" },
    plannerEmptyDayTitle = "No tasks for this day",
    plannerEmptyDaySubtitle = "Tap + to schedule a task here",
    plannerDoneTabSubtitle = "Complete some tasks first",
    plannerMonthAbbrevs = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"),
    plannerMonthFullNames = listOf("January","February","March","April","May","June","July","August","September","October","November","December"),
    plannerWeekDayHeaders = listOf("Mo","Tu","We","Th","Fr","Sa","Su"),
    plannerDayOfWeekNames = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"),

    cryptoTitle = "Crypto",
    cryptoSearchPlaceholder = "Search coins...",
    cryptoMarketEmpty = "No coins found",
    cryptoMarketEmptySubtitle = "Try a different search",
    cryptoPortfolioEmpty = "Portfolio is empty",
    cryptoPortfolioEmptySubtitle = "Go to Market tab and add coins to your portfolio",
    cryptoWatchlistEmpty = "Watchlist is empty",
    cryptoWatchlistEmptySubtitle = "Tap a coin in the Market tab to add to watchlist",
    cryptoRemove = "Remove",
    cryptoChart1D = "1D",
    cryptoChart7D = "7D",
    cryptoChart1M = "1M",
    cryptoChart1Y = "1Y",
    cryptoAddToPortfolioButton = "Add to Portfolio",
    cryptoWatchlistButton = "Watchlist",
    cryptoAddToPortfolioDialog = { coinName -> "Add $coinName to Portfolio" },
    cryptoAmountLabel = "Amount",
    cryptoPurchasePriceLabel = "Purchase Price",
    cryptoAddButton = "Add",
    cryptoCancelButton = "Cancel",
)

val RussianStrings = AppStrings(
    tabDashboard = "Главная",
    tabPlanner = "Задачи",
    tabFinance = "Финансы",
    tabSettings = "Настройки",

    settingsTitle = "Настройки",
    settingsSubtitle = "Настройте интерфейс под себя",
    sectionAppearance = "🎨  Оформление",
    labelTheme = "Тема",
    themeDark = "Тёмная",
    themeLight = "Светлая",
    themeSystem = "Авто",
    sectionLanguage = "🌐  Язык",
    labelAppLanguage = "Язык приложения",
    sectionFinance = "💰  Финансы",
    labelBaseCurrency = "Валюта",
    labelCurrencySubtitle = "Используется в разделе финансов",
    sectionNotifications = "🔔  Уведомления",
    labelEnableNotif = "Включить уведомления",
    labelEnableNotifSub = "Главный переключатель",
    labelTaskReminders = "Напоминания о задачах",
    labelTaskRemindersSub = "Уведомления о сроках выполнения",
    labelFinanceAlerts = "Финансовые оповещения",
    labelFinanceAlertsSub = "Уведомления о бюджете и расходах",
    sectionAbout = "ℹ️  О приложении",
    aboutVersion = "Версия 1.0.0 · Kotlin Multiplatform",
    settingsAppTagline = "Ваш персональный помощник",
    settingsPremium = "✨ Премиум",
    settingsVersionLabel = "v1.0",

    tabProfile = "Профиль",
    profileEditTitle = "Изменить профиль",
    profileNameLabel = "Ваше имя",
    profileNameHint = "Введите имя",
    profileDefaultName = "Пользователь",
    profilePickAvatar = "Выберите аватар",
    profileLevelLabel = { n -> "Уровень $n" },
    profileScoreLabel = "Очки продуктивности",
    profileToNextLevel = { n -> "$n очк. до след. уровня" },
    profileStatBalance = "Баланс",
    profileStatTasks = "Задач сделано",
    profileStatHabits = "Привычки",
    profileStatGoals = "Целей достигнуто",
    profileStatStreak = "Лучшая серия",
    profileEditAction = "Изменить профиль",
    profileSettingsHeader = "Настройки",
    profileRankTitles = listOf("Новичок", "Исследователь", "Достигатор", "Про", "Чемпион", "Мастер", "Легенда"),

    plannerProgressReady = "Готов начать! 🚀",
    plannerProgressDone = "Всё выполнено! 🎉",
    plannerProgressAlmostThere = "Почти готово! 💪",
    plannerProgressHalfway = "Половина пути! ✨",
    plannerProgressKeepGoing = "Продолжай! 🔥",
    plannerProgressLetsGo = "Начнём! ✨",
    plannerAllTasksDone = "Все задачи выполнены!",
    plannerAddFirstTask = "Добавьте первую задачу",
    plannerTasksRemaining = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        when {
            mod100 in 11..19 -> "Осталось $n задач"
            mod10 == 1       -> "Осталась $n задача"
            mod10 in 2..4    -> "Осталось $n задачи"
            else             -> "Осталось $n задач"
        }
    },
    plannerDoneLabel = "готово",
    plannerLabelTotal = "Всего",
    plannerLabelDone = "Готово",
    plannerLabelLeft = "Осталось",
    plannerPriorityBreakdown = "По приоритету",

    plannerTabPending = "Активные",
    plannerTabDone = "Выполнено",
    plannerFabNewTask = "Новая задача",
    plannerSearchHint = "Поиск задач…",
    plannerEmptyPendingTitle = "Всё выполнено! 🎉",
    plannerEmptyPendingSubtitle = "Нажмите «Новая задача», чтобы добавить",
    plannerEmptyDoneTitle = "Нет выполненных задач",
    plannerEmptyDoneSubtitle = "Выполняйте задачи, чтобы увидеть их здесь",
    plannerAddTaskAction = "Добавить",

    addTaskTitle = "Новая задача ✨",
    addTaskSubtitle = "Что нужно сделать?",
    addTaskFieldTitle = "Название задачи",
    addTaskFieldNote = "Заметка (необязательно)",
    addTaskPriority = "Приоритет",
    addTaskDueDate = "Срок",
    addTaskDueNone = "Без срока",
    addTaskDueToday = "Сегодня",
    addTaskDueTomorrow = "Завтра",
    addTaskDueWeek = "На неделе",
    addTaskCategory = "Категория",
    addTaskCategoryNone = "Без категории",
    addTaskButton = "Создать задачу",
    addTaskErrorEmpty = "Введите название задачи",

    priorityLow = "Низкий",
    priorityMedium = "Средний",
    priorityHigh = "Высокий",
    priorityCritical = "Критичный",

    dashboardGreetingMorning = "Доброе утро",
    dashboardGreetingAfternoon = "Добрый день",
    dashboardGreetingEvening = "Добрый вечер",
    dashboardGreetingNight = "Доброй ночи",
    dashboardTasksToday = "Задачи сегодня",
    dashboardNoTasks = "Нет задач на сегодня",
    dashboardFinanceMonth = "Этот месяц",
    dashboardIncome = "Доходы",
    dashboardExpense = "Расходы",
    dashboardBalance = "Баланс",
    dashboardHabitsDoneLabel = "Привычки",
    dashboardTodaysHabits = "Привычки на сегодня",
    dashboardHabitsDoneCount = { done, total -> "$done/$total готово" },

    habitsTitle = "Привычки",
    habitsSubtitle = { done, total -> "$done/$total сделано сегодня" },
    habitsCompletionPerfect = "Идеальный день! 🌟",
    habitsCompletionGreat = "Отличный прогресс! 🔥",
    habitsCompletionKeepGoing = "Продолжай! 💪",
    habitsCompletionStart = "Начни свой день! ✨",
    habitsBestStreak = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "дней"
            mod10 == 1       -> "день"
            mod10 in 2..4    -> "дня"
            else             -> "дней"
        }
        "🔥 Лучшая серия: $n $word"
    },
    habitsDayStreak = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "дней"
            mod10 == 1       -> "день"
            mod10 in 2..4    -> "дня"
            else             -> "дней"
        }
        "🔥 $n $word подряд"
    },
    habitsEmptyTitle = "Нет привычек",
    habitsEmptySubtitle = "Формируй полезные привычки каждый день",
    habitFreqDaily = "Каждый день",
    habitFreqWeekdays = "Будни",
    habitFreqWeekends = "Выходные",

    addHabitTitle = "Новая привычка ✨",
    addHabitFieldName = "Название привычки",
    addHabitErrorEmpty = "Введите название",
    addHabitPickIcon = "Выберите иконку",
    addHabitColorLabel = "Цвет",
    addHabitFrequencyLabel = "Частота",
    addHabitCreateButton = "Создать привычку",

    goalsTitle = "Цели",
    goalsSubtitle = { completed, total -> "$completed/$total выполнено" },
    goalsActiveSection = "Активные",
    goalsCompletedSection = "Выполненные",
    goalsCompletionAll = "Все цели достигнуты! 🌟",
    goalsCompletionHalfway = "Половина пути! 🚀",
    goalsCompletionKeepPushing = "Не останавливайся! 💪",
    goalsCompletionSet = "Поставь цели! 🎯",
    goalsAchieved = { completed, total -> "$completed из $total целей достигнуто" },
    goalsActiveCount = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "активных"
            mod10 == 1       -> "активная"
            mod10 in 2..4    -> "активные"
            else             -> "активных"
        }
        "🔥 $n $word"
    },
    goalsUpdateProgress = "＋ Обновить прогресс",
    addGoalSheetTitle = "🎯 Новая цель",
    addGoalSheetSubtitle = "Чего вы хотите достичь?",
    addGoalFieldTitle = "Название цели",
    addGoalPlaceholderTitle = "например, Пробежать 100 км",
    addGoalErrorTitleEmpty = "Название не может быть пустым",
    addGoalFieldTarget = "Цель",
    addGoalFieldUnit = "Единица",
    addGoalCreateButton = "Создать цель →",
    updateGoalProgressLabel = "Новое значение прогресса:",
    updateGoalCurrentProgress = { current, target, unit -> "Текущий: $current / $target $unit" },
    updateGoalButton = "Обновить",
    cancelButton = "Отмена",
    goalsEmptyTitle = "Нет целей",
    goalsEmptySubtitle = "Поставь цель и отслеживай прогресс",

    financeTitle = "Финансы",
    financeSubtitle = "Управляйте деньгами",
    financeTotalBalance = "Общий баланс",
    financeIncomeLabel = "Доходы",
    financeExpensesLabel = "Расходы",
    financeTabAll = "Все",
    financeTabExpenses = "Расходы",
    financeTabIncome = "Доходы",
    financeExpenseBreakdown = "Разбивка расходов",
    financeTotalLabel = "итого",
    financeExpenseTrend = "Динамика расходов",
    financeRecords = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "записей"
            mod10 == 1       -> "запись"
            mod10 in 2..4    -> "записи"
            else             -> "записей"
        }
        "$n $word"
    },
    financeEmptyTitle = "Нет транзакций",
    financeEmptySubtitle = "Нажмите + чтобы добавить первую транзакцию",
    financeAmountLabel = "Сумма *",
    financeAmountError = "Введите корректную сумму",
    financeCategoryLabel = "Категория *",
    financeNoteLabel = "Заметка (необязательно)",
    financeAddTitleIncome = "Доход",
    financeAddTitleExpense = "Расход",

    financeAdjustTitle = "Укажите баланс",
    financeAdjustHint = "Введите, сколько у вас сейчас денег — баланс будет учитывать доходы и расходы.",
    saveButton = "Сохранить",

    goalAffectsBalanceBadge = "Влияет на баланс",
    goalPurchasedBadge = "Куплено",
    goalUndoPurchase = "Отменить",
    goalMarkPurchased = "Отметить купленным",
    goalPurchaseToggle = "Цель-покупка",
    goalPurchaseToggleHint = "Спишет цену с баланса при покупке",
    goalPriceLabel = "Цена",

    plannerScreenTitle = "Планировщик",
    plannerViewYear = "Год",
    plannerViewMonth = "Месяц",
    plannerViewDay = "День",

    plannerNoTasksMonth = "Нет задач",
    plannerTasksCountMonth = { n ->
        val mod10 = n % 10; val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "задач"
            mod10 == 1       -> "задача"
            mod10 in 2..4    -> "задачи"
            else             -> "задач"
        }
        "$n $word"
    },
    plannerTasksDoneOf = { done, total -> "$done из $total задач выполнено" },
    plannerEmptyDayTitle = "Нет задач на этот день",
    plannerEmptyDaySubtitle = "Нажмите + чтобы добавить задачу",
    plannerDoneTabSubtitle = "Сначала выполните задачи",
    plannerMonthAbbrevs = listOf("Янв","Фев","Мар","Апр","Май","Июн","Июл","Авг","Сен","Окт","Ноя","Дек"),
    plannerMonthFullNames = listOf("Январь","Февраль","Март","Апрель","Май","Июнь","Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"),
    plannerWeekDayHeaders = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс"),
    plannerDayOfWeekNames = listOf("Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"),

    cryptoTitle = "Крипто",
    cryptoSearchPlaceholder = "Поиск монет...",
    cryptoMarketEmpty = "Монеты не найдены",
    cryptoMarketEmptySubtitle = "Попробуйте другой запрос",
    cryptoPortfolioEmpty = "Портфель пуст",
    cryptoPortfolioEmptySubtitle = "Перейдите в Рынок и добавьте монеты в портфель",
    cryptoWatchlistEmpty = "Список наблюдения пуст",
    cryptoWatchlistEmptySubtitle = "Нажмите на монету на вкладке Рынок, чтобы добавить",
    cryptoRemove = "Удалить",
    cryptoChart1D = "1Д",
    cryptoChart7D = "7Д",
    cryptoChart1M = "1М",
    cryptoChart1Y = "1Г",
    cryptoAddToPortfolioButton = "В портфель",
    cryptoWatchlistButton = "Наблюдение",
    cryptoAddToPortfolioDialog = { coinName -> "Добавить $coinName в портфель" },
    cryptoAmountLabel = "Количество",
    cryptoPurchasePriceLabel = "Цена покупки",
    cryptoAddButton = "Добавить",
    cryptoCancelButton = "Отмена",
)

val LocalStrings = compositionLocalOf<AppStrings> { EnglishStrings }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.RUSSIAN -> RussianStrings
}
