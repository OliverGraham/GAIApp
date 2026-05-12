package com.gainus.gaiapp.usecases

import com.gainus.gaiapp.Task
import com.gainus.gaiapp.TaskRepository

class AddTaskUseCase(
    private val taskRepository: TaskRepository,
    private val uuidGenerator: () -> String,
) {
    operator fun invoke(title: String) {
        if (title.isNotBlank()) {
            val newTask = Task(id = uuidGenerator(), title = title, isDone = false)
            taskRepository.addTask(newTask)
        }
    }
}
