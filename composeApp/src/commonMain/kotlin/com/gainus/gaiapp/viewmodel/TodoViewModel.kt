package com.gainus.gaiapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gainus.gaiapp.Task
import com.gainus.gaiapp.usecases.AddTaskUseCase
import com.gainus.gaiapp.usecases.DeleteTaskUseCase
import com.gainus.gaiapp.usecases.ObserveTasksUseCase
import com.gainus.gaiapp.usecases.ToggleTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoUiState(val tasks: List<Task> = emptyList(), val taskTitle: String = "")

class TodoViewModel(
    private val observeTasks: ObserveTasksUseCase,
    private val addTask: AddTaskUseCase,
    private val toggleTask: ToggleTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasks().collect { tasks -> _uiState.update { it.copy(tasks = tasks) } }
        }
    }

    fun onTaskTitleChange(newTitle: String) {
        _uiState.update { it.copy(taskTitle = newTitle) }
    }

    fun addTask() {
        viewModelScope.launch {
            addTask.invoke(_uiState.value.taskTitle)
            _uiState.update { it.copy(taskTitle = "") }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch { toggleTask.invoke(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { deleteTask.invoke(task) }
    }
}
