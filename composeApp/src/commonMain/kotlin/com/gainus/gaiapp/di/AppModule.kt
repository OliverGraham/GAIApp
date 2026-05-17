package com.gai.gaiapp.di

import com.gai.gaiapp.data.local.database.AppDatabase
import com.gai.gaiapp.data.local.database.provideDatabase
import com.gai.gaiapp.data.local.repository.RoomTaskRepository
import com.gai.gaiapp.domain.repository.TaskRepository
import com.gai.gaiapp.domain.usecase.AddTaskUseCase
import com.gai.gaiapp.domain.usecase.DeleteTaskUseCase
import com.gai.gaiapp.domain.usecase.ObserveTasksUseCase
import com.gai.gaiapp.domain.usecase.ToggleTaskUseCase
import com.gai.gaiapp.presentation.viewmodel.TodoViewModel
import com.gai.gaiapp.util.randomUUID
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
    // Database & Repository
    single { provideDatabase(get()) }
    single { get<AppDatabase>().taskDao() }
    singleOf(::RoomTaskRepository) bind TaskRepository::class

    // Use Cases
    factoryOf(::ObserveTasksUseCase)
    factoryOf(::AddTaskUseCase)
    factoryOf(::ToggleTaskUseCase)
    factoryOf(::DeleteTaskUseCase)

    // ViewModels
    factoryOf(::TodoViewModel)

    // Misc
    factory<() -> String> { { randomUUID() } }
}
