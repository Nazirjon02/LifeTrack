package tj.mahram.lifetrack.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val color: String,
    val isCustom: Boolean
)

enum class CategoryType {
    TASK, EXPENSE, INCOME
}

object DefaultCategories {
    val taskCategories = listOf(
        Category("task_work", "Work", CategoryType.TASK, "💼", "#4F46E5", false),
        Category("task_health", "Health", CategoryType.TASK, "🏃", "#10B981", false),
        Category("task_finance", "Finance", CategoryType.TASK, "💰", "#F59E0B", false),
        Category("task_personal", "Personal", CategoryType.TASK, "⭐", "#7C3AED", false)
    )

    val expenseCategories = listOf(
        Category("exp_food", "Food", CategoryType.EXPENSE, "🍔", "#EF4444", false),
        Category("exp_transport", "Transport", CategoryType.EXPENSE, "🚗", "#F97316", false),
        Category("exp_housing", "Housing", CategoryType.EXPENSE, "🏠", "#14B8A6", false),
        Category("exp_health", "Health", CategoryType.EXPENSE, "💊", "#10B981", false),
        Category("exp_entertainment", "Entertainment", CategoryType.EXPENSE, "🎮", "#8B5CF6", false),
        Category("exp_clothing", "Clothing", CategoryType.EXPENSE, "👗", "#EC4899", false),
        Category("exp_other", "Other", CategoryType.EXPENSE, "📦", "#6B7280", false)
    )

    val incomeCategories = listOf(
        Category("inc_salary", "Salary", CategoryType.INCOME, "💼", "#10B981", false),
        Category("inc_freelance", "Freelance", CategoryType.INCOME, "💻", "#3B82F6", false),
        Category("inc_investment", "Investment", CategoryType.INCOME, "📈", "#F59E0B", false),
        Category("inc_crypto", "Crypto", CategoryType.INCOME, "₿", "#F97316", false),
        Category("inc_other", "Other", CategoryType.INCOME, "💵", "#6B7280", false)
    )

    val all = taskCategories + expenseCategories + incomeCategories
}
