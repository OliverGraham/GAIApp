package com.gai.gaiapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gai.gaiapp.domain.model.Task
import com.gai.gaiapp.domain.usecase.AddTaskUseCase
import com.gai.gaiapp.domain.usecase.DeleteTaskUseCase
import com.gai.gaiapp.domain.usecase.ObserveTasksUseCase
import com.gai.gaiapp.domain.usecase.ToggleTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val taskTitle: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

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
            _uiState.update { it.copy(isLoading = true) }
            try {
                observeTasks().collect { tasks ->
                    _uiState.update {
                        it.copy(tasks = tasks, isLoading = false, errorMessage = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to load tasks")
                }
            }
        }
    }

    fun onTaskTitleChange(newTitle: String) {
        _uiState.update { it.copy(taskTitle = newTitle) }
    }

    fun addTask() {
        val title = _uiState.value.taskTitle
        if (title.isBlank()) return

        viewModelScope.launch {
            try {
                addTask.invoke(title)
                _uiState.update { it.copy(taskTitle = "", errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add task") }
            }
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            try {
                toggleTask.invoke(task)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to toggle task") }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                deleteTask.invoke(task)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete task") }
            }
        }
    }
}
