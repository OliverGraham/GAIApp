package com.gainus.gaiapp.usecases

import com.gainus.gaiapp.Task
import com.gainus.gaiapp.TaskRepository

class DeleteTaskUseCase(private val taskRepository: TaskRepository) {
    operator fun invoke(task: Task) {
        taskRepository.deleteTask(task)
    }
}
