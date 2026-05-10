package com.gainus.gaiapp

data class Task(val id: String, val title: String, val isDone: Boolean)

fun createSampleTasks(): List<Task> {
    return listOf(
        Task("1", "Buy groceries", false),
        Task("2", "Finish report", true),
        Task("3", "Call mom", false)
    )
}
