package tj.mahram.lifetrack.feature.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.getKoin
import tj.mahram.lifetrack.ui.components.EmptyState
import tj.mahram.lifetrack.ui.components.TaskCard
import tj.mahram.lifetrack.ui.components.TaskCardSkeleton

class PlannerScreen : Screen {
    @Composable
    override fun Content() {
        val koin = getKoin()
        val screenModel = rememberScreenModel<PlannerScreenModel> { koin.get() }
        val state by screenModel.state.collectAsState()
        PlannerContent(state = state, onIntent = screenModel::handleIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerContent(state: PlannerState, onIntent: (PlannerIntent) -> Unit) {
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Planner") },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                AnimatedVisibility(showSearch) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onIntent(PlannerIntent.Search(it)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        placeholder = { Text("Search tasks...") },
                        singleLine = true
                    )
                }
                // Category filter
                if (state.categories.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = state.categories.indexOfFirst { it.id == state.selectedCategoryId } + 1,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.background,
                        divider = {}
                    ) {
                        Tab(
                            selected = state.selectedCategoryId == null,
                            onClick = { onIntent(PlannerIntent.SelectCategory(null)) },
                            text = { Text("All") }
                        )
                        state.categories.forEach { cat ->
                            Tab(
                                selected = state.selectedCategoryId == cat.id,
                                onClick = { onIntent(PlannerIntent.SelectCategory(cat.id)) },
                                text = { Text("${cat.icon} ${cat.name}") }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(PlannerIntent.ShowAddTask) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            LazyColumn(contentPadding = padding) {
                items(5) { TaskCardSkeleton() }
            }
            return@Scaffold
        }

        if (state.filteredTasks.isEmpty()) {
            EmptyState(
                emoji = "✅",
                title = "No tasks yet",
                subtitle = "Tap + to create your first task",
                actionLabel = "Add Task",
                onAction = { onIntent(PlannerIntent.ShowAddTask) },
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp
            )
        ) {
            if (state.pendingTasks.isNotEmpty()) {
                item {
                    Text(
                        "Pending (${state.pendingTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(state.pendingTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { done -> onIntent(PlannerIntent.ToggleTask(task.id, done)) },
                        onDelete = { onIntent(PlannerIntent.DeleteTask(task.id)) },
                        onClick = {}
                    )
                }
            }
            if (state.completedTasks.isNotEmpty()) {
                item {
                    Text(
                        "Completed (${state.completedTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                items(state.completedTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { done -> onIntent(PlannerIntent.ToggleTask(task.id, done)) },
                        onDelete = { onIntent(PlannerIntent.DeleteTask(task.id)) },
                        onClick = {}
                    )
                }
            }
        }

        if (state.showAddTaskSheet) {
            AddTaskSheet(
                categories = state.categories,
                onDismiss = { onIntent(PlannerIntent.HideAddTask) },
                onConfirm = onIntent
            )
        }
    }
}
