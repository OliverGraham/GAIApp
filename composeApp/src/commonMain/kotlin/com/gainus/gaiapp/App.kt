package com.gainus.gaiapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import gaiapp.composeapp.generated.resources.Res
import gaiapp.composeapp.generated.resources.compose_multiplatform

import androidx.compose.ui.unit.sp
import com.gainus.gaiapp.usecases.AddTaskUseCase
import com.gainus.gaiapp.usecases.DeleteTaskUseCase
import com.gainus.gaiapp.usecases.ObserveTasksUseCase
import com.gainus.gaiapp.usecases.ToggleTaskUseCase


@Composable
@Preview
fun TodoListScreen() {
    // Initialize repository and use cases
    val taskRepository = remember { InMemoryTaskRepository() }
    val observeTasks = remember { ObserveTasksUseCase(taskRepository) }
    val addTask = remember { AddTaskUseCase(taskRepository, uuidGenerator = { randomUUID() }) }
    val toggleTask = remember { ToggleTaskUseCase(taskRepository) }
    val deleteTask = remember { DeleteTaskUseCase(taskRepository) }

    val tasks by observeTasks().collectAsState(initial = emptyList())

    var taskTitle by remember { mutableStateOf("") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxSize()
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Todo List",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )

            // Input field and Add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        addTask(taskTitle)
                        taskTitle = ""
                    }
                ) {
                    Text("Add")
                }
            }

            // Task list
            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks yet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { toggleTask(task) }
                                )
                                Text(
                                    text = task.title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    TodoListScreen()
}
