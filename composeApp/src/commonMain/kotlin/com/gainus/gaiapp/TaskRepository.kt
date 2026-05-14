package com.gainus.gaiapp

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>

    suspend fun addTask(task: Task)

    suspend fun toggleTask(task: Task)

    suspend fun deleteTask(task: Task)
}
