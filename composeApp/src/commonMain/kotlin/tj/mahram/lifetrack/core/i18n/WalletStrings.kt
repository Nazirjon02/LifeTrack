package tj.mahram.lifetrack.core.i18n

import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.WalletType

/**
 * Localized strings for the multi-wallet feature (wallet list, create/edit
 * sheet, deposit detail, and the picker in the add-transaction sheet). Kept as
 * its own data class — not folded into AppStrings — so its generated constructor
 * stays small (see the AppStrings 255-argument Dalvik limit note in memory).
 */
data class WalletStrings(
    // Finance entry card
    val entryTitle: String,
    val entrySubtitle: String,
    val entryEmptySubtitle: String,
    // Wallets screen
    val screenTitle: String,
    val screenSubtitle: String,
    val totalLabel: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val addTitle: String,
    val editTitle: String,
    // Types
    val typeCash: String,
    val typeCard: String,
    val typeSavings: String,
    val typeDeposit: String,
    // Create / edit sheet
    val nameLabel: String,
    val namePlaceholder: String,
    val nameError: String,
    val typeFieldLabel: String,
    val balanceLabel: String,
    val principalLabel: String,
    val colorLabel: String,
    val iconLabel: String,
    val createButton: String,
    // Deposit
    val depositSection: String,
    val rateLabel: String,
    val rateError: String,
    val termLabel: String,
    val term3Months: String,
    val term6Months: String,
    val term1Year: String,
    val term2Years: String,
    val perYear: String,
    val accruedLabel: String,
    val maturityPayoutLabel: String,
    val maturityDateLabel: String,
    val startedLabel: String,
    val termProgressLabel: String,
    val maturedBadge: String,
    // Detail transactions
    val detailTransactions: String,
    val detailNoTransactions: String,
    // Delete
    val deleteTitle: String,
    val deleteMessage: String,
    // Picker
    val pickerLabel: String,
    val pickerNone: String,
    // Shared actions (kept here to avoid touching AppStrings' DEX-limited ctor)
    val editButton: String,
    val deleteButton: String,
    // Plurals / dynamic (kept as nested words so callers don't hardcode)
    private val countSuffixEn: Boolean = true
) {
    fun typeLabel(type: WalletType): String = when (type) {
        WalletType.CASH -> typeCash
        WalletType.CARD -> typeCard
        WalletType.SAVINGS -> typeSavings
        WalletType.DEPOSIT -> typeDeposit
    }

    fun countText(n: Int): String =
        if (countSuffixEn) "$n ${if (n == 1) "wallet" else "wallets"}" else "Кошельков: $n"

    fun daysLeftText(n: Int): String =
        if (countSuffixEn) "$n ${if (n == 1) "day left" else "days left"}" else "Осталось $n дн."

    fun rateText(rate: Double): String {
        val r = if (rate % 1.0 == 0.0) rate.toLong().toString() else rate.toString()
        return "$r% $perYear"
    }
}

private val WalletStringsEn = WalletStrings(
    entryTitle = "Wallets",
    entrySubtitle = "Cash, cards & deposits",
    entryEmptySubtitle = "Add your first wallet",
    screenTitle = "Wallets",
    screenSubtitle = "All your money in one place",
    totalLabel = "Total across wallets",
    emptyTitle = "No wallets yet",
    emptySubtitle = "Create a wallet to track cash, cards and deposits.",
    addTitle = "New wallet",
    editTitle = "Edit wallet",
    typeCash = "Cash",
    typeCard = "Card",
    typeSavings = "Savings",
    typeDeposit = "Deposit",
    nameLabel = "Name",
    namePlaceholder = "e.g. Main card",
    nameError = "Enter a name",
    typeFieldLabel = "Type",
    balanceLabel = "Current balance",
    principalLabel = "Deposit amount",
    colorLabel = "Color",
    iconLabel = "Icon",
    createButton = "Create wallet",
    depositSection = "Deposit details",
    rateLabel = "Annual interest rate (%)",
    rateError = "Enter a valid rate",
    termLabel = "Term",
    term3Months = "3 mo",
    term6Months = "6 mo",
    term1Year = "1 yr",
    term2Years = "2 yr",
    perYear = "/ yr",
    accruedLabel = "Interest earned so far",
    maturityPayoutLabel = "Payout at maturity",
    maturityDateLabel = "Matures on",
    startedLabel = "Opened on",
    termProgressLabel = "Term progress",
    maturedBadge = "Matured",
    detailTransactions = "Transactions",
    detailNoTransactions = "No transactions in this wallet yet.",
    deleteTitle = "Delete wallet?",
    deleteMessage = "Its transactions are kept but will no longer belong to a wallet.",
    pickerLabel = "Wallet",
    pickerNone = "None",
    editButton = "Edit",
    deleteButton = "Delete",
    countSuffixEn = true
)

private val WalletStringsRu = WalletStrings(
    entryTitle = "Кошельки",
    entrySubtitle = "Наличные, карты и вклады",
    entryEmptySubtitle = "Добавьте первый кошелёк",
    screenTitle = "Кошельки",
    screenSubtitle = "Все ваши средства в одном месте",
    totalLabel = "Всего по кошелькам",
    emptyTitle = "Пока нет кошельков",
    emptySubtitle = "Создайте кошелёк для учёта наличных, карт и вкладов.",
    addTitle = "Новый кошелёк",
    editTitle = "Изменить кошелёк",
    typeCash = "Наличные",
    typeCard = "Карта",
    typeSavings = "Сбережения",
    typeDeposit = "Вклад",
    nameLabel = "Название",
    namePlaceholder = "напр. Основная карта",
    nameError = "Введите название",
    typeFieldLabel = "Тип",
    balanceLabel = "Текущий баланс",
    principalLabel = "Сумма вклада",
    colorLabel = "Цвет",
    iconLabel = "Значок",
    createButton = "Создать кошелёк",
    depositSection = "Детали вклада",
    rateLabel = "Годовая ставка (%)",
    rateError = "Введите корректную ставку",
    termLabel = "Срок",
    term3Months = "3 мес",
    term6Months = "6 мес",
    term1Year = "1 год",
    term2Years = "2 года",
    perYear = "/ год",
    accruedLabel = "Накоплено процентов",
    maturityPayoutLabel = "Выплата при закрытии",
    maturityDateLabel = "Дата закрытия",
    startedLabel = "Дата открытия",
    termProgressLabel = "Прогресс срока",
    maturedBadge = "Срок истёк",
    detailTransactions = "Транзакции",
    detailNoTransactions = "В этом кошельке пока нет транзакций.",
    deleteTitle = "Удалить кошелёк?",
    deleteMessage = "Транзакции сохранятся, но перестанут принадлежать кошельку.",
    pickerLabel = "Кошелёк",
    pickerNone = "Без кошелька",
    editButton = "Изменить",
    deleteButton = "Удалить",
    countSuffixEn = false
)

fun walletStringsFor(language: AppLanguage): WalletStrings = when (language) {
    AppLanguage.RUSSIAN -> WalletStringsRu
    else -> WalletStringsEn
}
