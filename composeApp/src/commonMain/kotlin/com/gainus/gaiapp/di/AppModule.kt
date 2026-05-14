package com.gainus.gaiapp.di

import com.gainus.gaiapp.RoomTaskRepository
import com.gainus.gaiapp.TaskRepository
import com.gainus.gaiapp.database.AppDatabase
import com.gainus.gaiapp.database.provideDatabase
import com.gainus.gaiapp.randomUUID
import com.gainus.gaiapp.usecases.AddTaskUseCase
import com.gainus.gaiapp.usecases.DeleteTaskUseCase
import com.gainus.gaiapp.usecases.ObserveTasksUseCase
import com.gainus.gaiapp.usecases.ToggleTaskUseCase
import com.gainus.gaiapp.viewmodel.TodoViewModel
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
