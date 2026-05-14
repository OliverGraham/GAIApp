package com.gainus.gaiapp.domain.usecase

import com.gainus.gaiapp.domain.model.Task
import com.gainus.gaiapp.domain.repository.TaskRepository

class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        taskRepository.deleteTask(task)
    }
}
