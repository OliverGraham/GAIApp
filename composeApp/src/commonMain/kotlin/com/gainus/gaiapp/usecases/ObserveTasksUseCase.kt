package com.gainus.gaiapp.usecases

import com.gainus.gaiapp.Task
import com.gainus.gaiapp.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.getTasks()
}
