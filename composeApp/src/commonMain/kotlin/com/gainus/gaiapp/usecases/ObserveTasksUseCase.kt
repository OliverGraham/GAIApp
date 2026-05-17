package com.gai.gaiapp.domain.usecase

import com.gai.gaiapp.domain.model.Task
import com.gai.gaiapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveTasksUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.getTasks()
}
