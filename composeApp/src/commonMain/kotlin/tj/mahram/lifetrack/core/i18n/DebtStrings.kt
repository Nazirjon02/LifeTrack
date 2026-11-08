package tj.mahram.lifetrack.core.i18n

/**
 * Localized strings for the Debts feature, held by [AppStrings.debts] (call
 * sites keep using `s.debtXxx` through delegating getters in AppStrings).
 *
 * Split out because AppStrings had grown to 249 constructor parameters: the
 * data class's synthetic `copy$default` then needs 259 argument slots, over
 * the JVM limit of 255, so the Desktop app failed to load AppStrings
 * ("ClassFormatError: Too many arguments in method signature"). Keep
 * AppStrings at 245 params or fewer — AppStringsJvmTest checks it.
 */
data class DebtStrings(
    val tabDebts: String,
    val debtsTitle: String,
    val debtsSubtitle: String,
    val debtOwedToMe: String,
    val debtIOwe: String,
    val debtNetPosition: String,
    val debtNetPositive: String,
    val debtNetNegative: String,
    val debtNetEven: String,
    val debtsFilterAll: String,
    val debtEmptyTitle: String,
    val debtEmptySubtitle: String,
    val debtAddLent: String,
    val debtAddBorrowed: String,
    val debtAddTitle: String,
    val debtDirectionLabel: String,
    val debtPersonLabel: String,
    val debtPersonPlaceholder: String,
    val debtAmountLabel: String,
    val debtNoteLabel: String,
    val debtDueLabel: String,
    val debtDueNone: String,
    val debtDueWeek: String,
    val debtDue2Weeks: String,
    val debtDueMonth: String,
    val debtDue3Months: String,
    val debtNameError: String,
    val debtCreateButton: String,
    val debtRepaidLabel: String,
    val debtRemainingWord: String,
    val debtDuePrefix: String,
    val debtOverdue: String,
    val debtSettledBadge: String,
    val debtActionPay: String,
    val debtActionSettle: String,
    val debtActionReopen: String,
    val debtPaymentTitle: String,
    val debtPaymentHint: String,
    val debtPaymentFull: String,
    val debtCount: (Int) -> String,
)

val DebtStringsEn = DebtStrings(
    tabDebts = "Debts",
    debtsTitle = "Debts",
    debtsSubtitle = "Who owes you, and whom you owe",
    debtOwedToMe = "Owed to me",
    debtIOwe = "I owe",
    debtNetPosition = "Net position",
    debtNetPositive = "Overall, you're owed",
    debtNetNegative = "Overall, you owe",
    debtNetEven = "All settled up",
    debtsFilterAll = "All",
    debtEmptyTitle = "No debts yet",
    debtEmptySubtitle = "Track money you lent out or borrowed",
    debtAddLent = "I lent",
    debtAddBorrowed = "I borrowed",
    debtAddTitle = "New debt",
    debtDirectionLabel = "Direction",
    debtPersonLabel = "Person *",
    debtPersonPlaceholder = "Name",
    debtAmountLabel = "Amount *",
    debtNoteLabel = "Note (optional)",
    debtDueLabel = "Due date",
    debtDueNone = "No date",
    debtDueWeek = "1 week",
    debtDue2Weeks = "2 weeks",
    debtDueMonth = "1 month",
    debtDue3Months = "3 months",
    debtNameError = "Enter a name",
    debtCreateButton = "Add debt",
    debtRepaidLabel = "Repaid",
    debtRemainingWord = "left",
    debtDuePrefix = "Due",
    debtOverdue = "Overdue",
    debtSettledBadge = "Settled",
    debtActionPay = "Payment",
    debtActionSettle = "Settle",
    debtActionReopen = "Reopen",
    debtPaymentTitle = "Record payment",
    debtPaymentHint = "How much was repaid?",
    debtPaymentFull = "Repay all",
    debtCount = { n -> if (n == 1) "1 debt" else "$n debts" },
)

val DebtStringsRu = DebtStrings(
    tabDebts = "Долги",
    debtsTitle = "Долги",
    debtsSubtitle = "Кто должен вам и кому должны вы",
    debtOwedToMe = "Мне должны",
    debtIOwe = "Я должен",
    debtNetPosition = "Чистая позиция",
    debtNetPositive = "В целом должны вам",
    debtNetNegative = "В целом должны вы",
    debtNetEven = "Всё в расчёте",
    debtsFilterAll = "Все",
    debtEmptyTitle = "Долгов пока нет",
    debtEmptySubtitle = "Отслеживайте, что вы дали или взяли в долг",
    debtAddLent = "Я дал",
    debtAddBorrowed = "Я взял",
    debtAddTitle = "Новый долг",
    debtDirectionLabel = "Направление",
    debtPersonLabel = "Кто *",
    debtPersonPlaceholder = "Имя",
    debtAmountLabel = "Сумма *",
    debtNoteLabel = "Заметка (необязательно)",
    debtDueLabel = "Срок возврата",
    debtDueNone = "Без срока",
    debtDueWeek = "1 неделя",
    debtDue2Weeks = "2 недели",
    debtDueMonth = "1 месяц",
    debtDue3Months = "3 месяца",
    debtNameError = "Введите имя",
    debtCreateButton = "Добавить долг",
    debtRepaidLabel = "Погашено",
    debtRemainingWord = "осталось",
    debtDuePrefix = "Срок",
    debtOverdue = "Просрочено",
    debtSettledBadge = "Закрыт",
    debtActionPay = "Платёж",
    debtActionSettle = "Закрыть",
    debtActionReopen = "Открыть",
    debtPaymentTitle = "Внести платёж",
    debtPaymentHint = "Сколько вернули?",
    debtPaymentFull = "Погасить всё",
    debtCount = { n ->
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod100 in 11..19 -> "долгов"
            mod10 == 1       -> "долг"
            mod10 in 2..4    -> "долга"
            else             -> "долгов"
        }
        "$n $word"
    },
)
