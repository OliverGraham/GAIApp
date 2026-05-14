package com.gainus.gaiapp

import com.gainus.gaiapp.data.local.database.TaskDao
import com.gainus.gaiapp.data.local.database.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTaskDao : TaskDao {
    private val tasksFlow = MutableStateFlow<List<TaskEntity>>(emptyList())

    override fun observeAllTasks(): Flow<List<TaskEntity>> = tasksFlow

    override suspend fun insertTask(task: TaskEntity) {
        tasksFlow.value = tasksFlow.value.filter { it.id != task.id } + task
    }

    override suspend fun updateTask(task: TaskEntity) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun deleteTask(task: TaskEntity) {
        tasksFlow.value = tasksFlow.value.filter { it.id != task.id }
    }

    fun clear() {
        tasksFlow.value = emptyList()
    }

    fun seed(initialTasks: List<TaskEntity>) {
        tasksFlow.value = initialTasks
    }
}
