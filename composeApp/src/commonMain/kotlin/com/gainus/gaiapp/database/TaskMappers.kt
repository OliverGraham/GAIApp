package com.gai.gaiapp.data.local.database

import com.gai.gaiapp.domain.model.Task

// These mappers fulfill part of TODO-014: Add task database schema

fun TaskEntity.toDomain(): Task = Task(id = this.id, title = this.title, isDone = this.isDone)

fun Task.toEntity(): TaskEntity = TaskEntity(id = this.id, title = this.title, isDone = this.isDone)
