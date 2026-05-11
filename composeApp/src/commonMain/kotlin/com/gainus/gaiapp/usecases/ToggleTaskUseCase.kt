package com.gainus.gaiapp.usecases

import com.gainus.gaiapp.Task
import com.gainus.gaiapp.TaskRepository

class ToggleTaskUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task) {
        taskRepository.toggleTask(task)
    }
}
