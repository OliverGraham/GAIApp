package com.gainus.gaiapp.di

import com.gainus.gaiapp.InMemoryTaskRepository
import com.gainus.gaiapp.TaskRepository
import com.gainus.gaiapp.database.AppDatabase
import com.gainus.gaiapp.randomUUID
import com.gainus.gaiapp.usecases.AddTaskUseCase
import com.gainus.gaiapp.usecases.DeleteTaskUseCase
import com.gainus.gaiapp.usecases.ObserveTasksUseCase
import com.gainus.gaiapp.usecases.ToggleTaskUseCase
import com.gainus.gaiapp.viewmodel.TodoViewModel
import org.koin.dsl.module

val appModule = module {
    // Singletons
    single<TaskRepository> { InMemoryTaskRepository() } // Will be replaced in TODO-016
    factory<() -> String> { { randomUUID() } }
    single { get<AppDatabase>().taskDao() } // Provide TaskDao from AppDatabase

    // Use Cases
    factory { ObserveTasksUseCase(get()) }
    factory { AddTaskUseCase(get(), get()) }
    factory { ToggleTaskUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }

    // ViewModels
    factory { TodoViewModel(get(), get(), get(), get()) }
}
