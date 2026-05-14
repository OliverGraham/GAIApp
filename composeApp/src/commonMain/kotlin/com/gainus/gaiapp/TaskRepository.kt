package com.gainus.gaiapp.domain.repository

import com.gainus.gaiapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>

    suspend fun addTask(task: Task)

    suspend fun toggleTask(task: Task)

    suspend fun deleteTask(task: Task)
}
