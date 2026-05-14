package com.gainus.gaiapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeTaskRepository : TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    // For simulating errors
    var shouldThrowErrorOnAddTask: Boolean = false
    var shouldThrowErrorOnToggleTask: Boolean = false
    var shouldThrowErrorOnDeleteTask: Boolean = false
    var shouldThrowErrorOnObserveTasks: Boolean = false

    override fun getTasks(): Flow<List<Task>> {
        if (shouldThrowErrorOnObserveTasks) {
            return flow { throw Exception("Failed to load tasks") }
        }
        return _tasks
    }

    override suspend fun addTask(task: Task) {
        if (shouldThrowErrorOnAddTask) throw Exception("Failed to add task")
        _tasks.value = _tasks.value.filter { it.id != task.id } + task
    }

    override suspend fun toggleTask(task: Task) {
        if (shouldThrowErrorOnToggleTask) throw Exception("Failed to toggle task")
        _tasks.value =
            _tasks.value.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
    }

    override suspend fun deleteTask(task: Task) {
        if (shouldThrowErrorOnDeleteTask) throw Exception("Failed to delete task")
        _tasks.value = _tasks.value.filter { it.id != task.id }
    }

    fun clear() {
        _tasks.value = emptyList()
    }

    fun seed(initialTasks: List<Task>) {
        _tasks.value = initialTasks
    }
}
