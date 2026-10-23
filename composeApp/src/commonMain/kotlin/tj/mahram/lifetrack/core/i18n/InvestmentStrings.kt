package tj.mahram.lifetrack.core.i18n

import kotlin.math.abs
import tj.mahram.lifetrack.domain.model.AppLanguage
import tj.mahram.lifetrack.domain.model.InvestmentType

/**
 * Localized strings for the investments feature (portfolio list, create/edit
 * sheet, and the finance entry card). Kept as its own data class — not folded
 * into AppStrings — so its generated constructor stays small (see the AppStrings
 * 255-argument Dalvik limit note in memory).
 */
data class InvestmentStrings(
    // Finance entry card
    val entryTitle: String,
    val entrySubtitle: String,
    val entryEmptySubtitle: String,
    // Investments screen
    val screenTitle: String,
    val screenSubtitle: String,
    val portfolioValueLabel: String,
    val investedLabel: String,
    val currentValueLabel: String,
    val totalProfitLabel: String,
    val emptyTitle: String,
    val emptySubtitle: String,
    val addTitle: String,
    val editTitle: String,
    // Types
    val typeStock: String,
    val typeFund: String,
    val typeBusiness: String,
    val typeMetal: String,
    val typeRealEstate: String,
    val typeOther: String,
    // Create / edit sheet
    val nameLabel: String,
    val namePlaceholder: String,
    val nameError: String,
    val typeFieldLabel: String,
    val amountInvestedLabel: String,
    val amountError: String,
    val noteLabel: String,
    val notePlaceholder: String,
    val colorLabel: String,
    val iconLabel: String,
    val createButton: String,
    // Delete
    val deleteTitle: String,
    val deleteMessage: String,
    // Shared actions (kept here to avoid touching AppStrings' DEX-limited ctor)
    val editButton: String,
    val deleteButton: String,
    // Plurals / dynamic (kept as nested words so callers don't hardcode)
    private val countSuffixEn: Boolean = true
) {
    fun typeLabel(type: InvestmentType): String = when (type) {
        InvestmentType.STOCK -> typeStock
        InvestmentType.FUND -> typeFund
        InvestmentType.BUSINESS -> typeBusiness
        InvestmentType.METAL -> typeMetal
        InvestmentType.REAL_ESTATE -> typeRealEstate
        InvestmentType.OTHER -> typeOther
    }

    fun countText(n: Int): String =
        if (countSuffixEn) "$n ${if (n == 1) "holding" else "holdings"}" else "Активов: $n"

    /** A signed percentage like "+12.5%" / "-3%". */
    fun percentText(percent: Double): String {
        val rounded = (abs(percent) * 10).toLong() / 10.0
        val body = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
        val sign = if (percent >= 0) "+" else "−"
        return "$sign$body%"
    }
}

private val InvestmentStringsEn = InvestmentStrings(
    entryTitle = "Investments",
    entrySubtitle = "Stocks, funds & assets",
    entryEmptySubtitle = "Track your portfolio",
    screenTitle = "Investments",
    screenSubtitle = "Your whole portfolio in one place",
    portfolioValueLabel = "Portfolio value",
    investedLabel = "Invested",
    currentValueLabel = "Current value",
    totalProfitLabel = "Total profit / loss",
    emptyTitle = "No investments yet",
    emptySubtitle = "Add a holding to track its value and returns.",
    addTitle = "New investment",
    editTitle = "Edit investment",
    typeStock = "Stocks",
    typeFund = "Fund",
    typeBusiness = "Business",
    typeMetal = "Metals",
    typeRealEstate = "Real estate",
    typeOther = "Other",
    nameLabel = "Name",
    namePlaceholder = "e.g. Apple shares",
    nameError = "Enter a name",
    typeFieldLabel = "Type",
    amountInvestedLabel = "Amount invested",
    amountError = "Enter an amount",
    noteLabel = "Note (optional)",
    notePlaceholder = "e.g. 10 shares at \$150",
    colorLabel = "Color",
    iconLabel = "Icon",
    createButton = "Add investment",
    deleteTitle = "Delete investment?",
    deleteMessage = "This holding will be removed from your portfolio.",
    editButton = "Edit",
    deleteButton = "Delete",
    countSuffixEn = true
)

private val InvestmentStringsRu = InvestmentStrings(
    entryTitle = "Инвестиции",
    entrySubtitle = "Акции, фонды и активы",
    entryEmptySubtitle = "Отслеживайте портфель",
    screenTitle = "Инвестиции",
    screenSubtitle = "Весь ваш портфель в одном месте",
    portfolioValueLabel = "Стоимость портфеля",
    investedLabel = "Вложено",
    currentValueLabel = "Текущая стоимость",
    totalProfitLabel = "Общая прибыль / убыток",
    emptyTitle = "Пока нет инвестиций",
    emptySubtitle = "Добавьте актив, чтобы отслеживать его стоимость и доход.",
    addTitle = "Новая инвестиция",
    editTitle = "Изменить инвестицию",
    typeStock = "Акции",
    typeFund = "Фонд",
    typeBusiness = "Бизнес",
    typeMetal = "Металлы",
    typeRealEstate = "Недвижимость",
    typeOther = "Другое",
    nameLabel = "Название",
    namePlaceholder = "напр. Акции Apple",
    nameError = "Введите название",
    typeFieldLabel = "Тип",
    amountInvestedLabel = "Сумма вложений",
    amountError = "Введите сумму",
    noteLabel = "Заметка (необязательно)",
    notePlaceholder = "напр. 10 акций по \$150",
    colorLabel = "Цвет",
    iconLabel = "Значок",
    createButton = "Добавить инвестицию",
    deleteTitle = "Удалить инвестицию?",
    deleteMessage = "Этот актив будет удалён из вашего портфеля.",
    editButton = "Изменить",
    deleteButton = "Удалить",
    countSuffixEn = false
)

fun investmentStringsFor(language: AppLanguage): InvestmentStrings = when (language) {
    AppLanguage.RUSSIAN -> InvestmentStringsRu
    else -> InvestmentStringsEn
}
