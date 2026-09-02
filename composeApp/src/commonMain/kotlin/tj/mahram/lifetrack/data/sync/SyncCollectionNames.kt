package tj.mahram.lifetrack.data.sync

/** Firestore sub-collection names under `users/{uid}/`. Single source of truth. */
object SyncCollectionNames {
    const val CATEGORIES = "categories"
    const val TASKS = "tasks"
    const val TRANSACTIONS = "transactions"
    const val GOALS = "goals"
    const val DEBTS = "debts"
    const val PROBLEMS = "problems"
    const val PROBLEM_HISTORY = "problem_history"
}
