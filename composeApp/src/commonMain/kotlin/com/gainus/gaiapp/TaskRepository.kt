package com.gainus.gaiapp

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>

    fun addTask(task: Task)

    fun toggleTask(task: Task)

    fun deleteTask(task: Task)
}
