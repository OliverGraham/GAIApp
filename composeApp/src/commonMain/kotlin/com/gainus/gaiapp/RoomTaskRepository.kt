package com.gainus.gaiapp

import com.gainus.gaiapp.database.TaskDao
import com.gainus.gaiapp.database.toDomain
import com.gainus.gaiapp.database.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepository(private val taskDao: TaskDao) : TaskRepository {
    override fun getTasks(): Flow<List<Task>> =
        taskDao.observeAllTasks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun toggleTask(task: Task) {
        taskDao.updateTask(task.copy(isDone = !task.isDone).toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }
}
