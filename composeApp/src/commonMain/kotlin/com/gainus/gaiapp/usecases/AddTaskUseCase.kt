package com.gai.gaiapp.domain.usecase

import com.gai.gaiapp.domain.model.Task
import com.gai.gaiapp.domain.repository.TaskRepository

class AddTaskUseCase(
    private val taskRepository: TaskRepository,
    private val uuidGenerator: () -> String,
) {
    suspend operator fun invoke(title: String) {
        if (title.isNotBlank()) {
            val newTask = Task(id = uuidGenerator(), title = title, isDone = false)
            taskRepository.addTask(newTask)
        }
    }
}
