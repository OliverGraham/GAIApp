package com.gainus.gaiapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository : TaskRepository {

    private val _tasks = MutableStateFlow(createSampleTasks())

    override fun getTasks(): Flow<List<Task>> = _tasks

    override fun addTask(task: Task) {
        _tasks.update { currentTasks -> currentTasks + task }
    }

    override fun toggleTask(task: Task) {
        _tasks.update { currentTasks ->
            currentTasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
        }
    }

    override fun deleteTask(task: Task) {
        _tasks.update { currentTasks -> currentTasks.filter { it.id != task.id } }
    }

    private fun createSampleTasks(): List<Task> =
        listOf(
            Task(id = randomUUID(), title = "Sample Task 1", isDone = false),
            Task(id = randomUUID(), title = "Sample Task 2", isDone = true),
        )
}
