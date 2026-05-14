package com.gainus.gaiapp.di

import com.gainus.gaiapp.data.local.database.AppDatabase
import com.gainus.gaiapp.data.local.database.provideDatabase
import com.gainus.gaiapp.data.local.repository.RoomTaskRepository
import com.gainus.gaiapp.domain.repository.TaskRepository
import com.gainus.gaiapp.domain.usecase.AddTaskUseCase
import com.gainus.gaiapp.domain.usecase.DeleteTaskUseCase
import com.gainus.gaiapp.domain.usecase.ObserveTasksUseCase
import com.gainus.gaiapp.domain.usecase.ToggleTaskUseCase
import com.gainus.gaiapp.presentation.viewmodel.TodoViewModel
import com.gainus.gaiapp.util.randomUUID
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
