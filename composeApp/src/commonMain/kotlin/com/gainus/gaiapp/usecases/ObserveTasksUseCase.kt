package com.gainus.gaiapp.domain.usecase

import com.gainus.gaiapp.domain.model.Task
import com.gainus.gaiapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.getTasks()
}
