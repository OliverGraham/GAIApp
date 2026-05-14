package com.gainus.gaiapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainus.gaiapp.viewmodel.TodoUiState
import com.gainus.gaiapp.viewmodel.TodoViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListScreen(
    uiState: TodoUiState,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
) {
    MaterialTheme {
        Column(
            modifier =
                Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    .fillMaxSize()
                    .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Todo List",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Total tasks: ${uiState.tasks.size}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }

            // Input field and Add button
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = uiState.taskTitle,
                    onValueChange = { onTaskTitleChange(it) },
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { onAddTask() }) { Text("Add") }
            }

            // Task list
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tasks.isEmpty()) {
                Text(
                    text = "No tasks yet",
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                            colors =
                                CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { onToggleTask(task) },
                                )
                                Text(
                                    text = task.title,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                )
                                Button(onClick = { onDeleteTask(task) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoListScreen(viewModel: TodoViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    TodoListScreen(
        uiState = uiState,
        onTaskTitleChange = viewModel::onTaskTitleChange,
        onAddTask = viewModel::addTask,
        onToggleTask = viewModel::toggleTask,
        onDeleteTask = viewModel::deleteTask,
    )
}

@Preview
@Composable
fun TodoListScreenLoadingPreview() {
    TodoListScreen(
        uiState = TodoUiState(isLoading = true),
        onTaskTitleChange = {},
        onAddTask = {},
        onToggleTask = {},
        onDeleteTask = {},
    )
}

@Preview
@Composable
fun TodoListScreenErrorPreview() {
    TodoListScreen(
        uiState = TodoUiState(errorMessage = "Failed to load tasks"),
        onTaskTitleChange = {},
        onAddTask = {},
        onToggleTask = {},
        onDeleteTask = {},
    )
}

@Preview
@Composable
fun TodoListScreenWithTasksPreview() {
    TodoListScreen(
        uiState =
            TodoUiState(
                tasks =
                    listOf(
                        Task("1", "Buy groceries", false),
                        Task("2", "Walk the dog", true),
                        Task("3", "Finish report", false),
                    )
            ),
        onTaskTitleChange = {},
        onAddTask = {},
        onToggleTask = {},
        onDeleteTask = {},
    )
}

@Composable
fun App() {
    TodoListScreen()
}
