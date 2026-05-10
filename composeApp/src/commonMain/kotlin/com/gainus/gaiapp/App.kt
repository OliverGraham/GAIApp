package com.gainus.gaiapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateListOf

@Composable
@Preview
fun TodoListScreen() {
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
                var taskTitle by remember { mutableStateOf("") }
                var taskIdCounter by remember { mutableStateOf(3) }

                BasicTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            val newTask = Task(
                                id = "new-$taskIdCounter",
                                title = taskTitle,
                                isDone = false
                            )
                            tasks.add(newTask)
                            taskIdCounter++
                            taskTitle = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }

            // Task list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(tasks) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (task.isDone) {
                            Image(
                                painterResource(Res.drawable.compose_multiplatform),
                                contentDescription = "Checkmark",
                                modifier = Modifier.size(24.dp)
                            )
                        }
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

@Composable
fun App() {
    TodoListScreen()
}

// Initialize the task list with sample data
val tasks = mutableStateListOf<Task>().apply {
    addAll(createSampleTasks())
}
