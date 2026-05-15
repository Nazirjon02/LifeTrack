package tj.mahram.lifetrack.core.i18n

import androidx.compose.runtime.compositionLocalOf
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.TaskPriority

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
    val plannerDoneLabel: String,     // label inside the ring: "done"
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
) {
    fun priorityLabel(priority: TaskPriority): String = when (priority) {
        TaskPriority.LOW      -> priorityLow
        TaskPriority.MEDIUM   -> priorityMedium
        TaskPriority.HIGH     -> priorityHigh
        TaskPriority.CRITICAL -> priorityCritical
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
    dashboardTasksToday = "Today's Tasks",
    dashboardNoTasks = "No tasks for today",
    dashboardFinanceMonth = "This Month",
    dashboardIncome = "Income",
    dashboardExpense = "Expense",
    dashboardBalance = "Balance",
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
            mod100 in 11..19          -> "Осталось $n задач"
            mod10 == 1                -> "Осталась $n задача"
            mod10 in 2..4             -> "Осталось $n задачи"
            else                      -> "Осталось $n задач"
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
    dashboardTasksToday = "Задачи на сегодня",
    dashboardNoTasks = "Нет задач на сегодня",
    dashboardFinanceMonth = "Этот месяц",
    dashboardIncome = "Доходы",
    dashboardExpense = "Расходы",
    dashboardBalance = "Баланс",
)

val LocalStrings = compositionLocalOf<AppStrings> { EnglishStrings }

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.RUSSIAN -> RussianStrings
}
