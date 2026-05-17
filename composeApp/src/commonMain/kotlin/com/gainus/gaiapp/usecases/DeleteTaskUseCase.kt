package com.gai.gaiapp.domain.usecase

import com.gai.gaiapp.domain.model.Task
import com.gai.gaiapp.domain.repository.TaskRepository

class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        taskRepository.deleteTask(task)
    }
}
