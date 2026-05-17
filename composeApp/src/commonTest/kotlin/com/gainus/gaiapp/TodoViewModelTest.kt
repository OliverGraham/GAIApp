package com.gai.gaiapp

import com.gai.gaiapp.domain.model.Task
import com.gai.gaiapp.domain.usecase.AddTaskUseCase
import com.gai.gaiapp.domain.usecase.DeleteTaskUseCase
import com.gai.gaiapp.domain.usecase.ObserveTasksUseCase
import com.gai.gaiapp.domain.usecase.ToggleTaskUseCase
import com.gai.gaiapp.presentation.viewmodel.TodoViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TodoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var toggleTaskUseCase: ToggleTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var observeTasksUseCase: ObserveTasksUseCase
    private lateinit var viewModel: TodoViewModel

    private var uuidCounter = 0
    private val fakeUuidGenerator: () -> String = { "test-uuid-${uuidCounter++}" }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTaskRepository = FakeTaskRepository()
        observeTasksUseCase = ObserveTasksUseCase(fakeTaskRepository)
        uuidCounter = 0 // Reset counter for each test
        addTaskUseCase = AddTaskUseCase(fakeTaskRepository, fakeUuidGenerator)
        toggleTaskUseCase = ToggleTaskUseCase(fakeTaskRepository)
        deleteTaskUseCase = DeleteTaskUseCase(fakeTaskRepository)
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        // Allow the viewModel's init block (observeTasks().collect) to run and emit the initial
        // state.
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows loading then loads tasks`() = runTest {
        val initialTasks = listOf(Task("1", "Task 1", false))
        fakeTaskRepository.seed(initialTasks)

        // Re-initialize ViewModel to pick up seeded tasks and trigger init block again
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )

        // Immediately after ViewModel creation, isLoading should be true
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle() // Allow init block to complete

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(initialTasks, uiState.tasks)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `onTaskTitleChange updates taskTitle in UI state`() = runTest {
        val newTitle = "New Task Title"
        viewModel.onTaskTitleChange(newTitle)
        assertEquals(newTitle, viewModel.uiState.value.taskTitle)
    }

    @Test
    fun `onTaskTitleChange with empty string clears taskTitle`() = runTest {
        viewModel.onTaskTitleChange("Some initial title")
        assertEquals("Some initial title", viewModel.uiState.value.taskTitle)

        viewModel.onTaskTitleChange("")
        assertEquals("", viewModel.uiState.value.taskTitle)
    }

    @Test
    fun `addTask adds a new task and clears taskTitle`() = runTest {
        val initialTitle = "New Task"
        viewModel.onTaskTitleChange(initialTitle)
        viewModel.addTask()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.tasks.size)
        assertEquals(initialTitle, uiState.tasks.first().title)
        assertFalse(uiState.tasks.first().isDone)
        assertEquals("test-uuid-0", uiState.tasks.first().id) // Check generated UUID
        assertTrue(uiState.taskTitle.isEmpty())
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `addTask does not add a blank task`() = runTest {
        viewModel.onTaskTitleChange("   ") // Blank title
        viewModel.addTask()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.tasks.isEmpty())
        assertEquals("   ", uiState.taskTitle) // Title should not be cleared if not added
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `toggleTask changes task completion status`() = runTest {
        val task = Task("1", "Test Task", false)
        fakeTaskRepository.seed(listOf(task))

        // Re-initialize ViewModel to pick up seeded tasks
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        advanceUntilIdle()

        val initialUiState = viewModel.uiState.value
        assertFalse(initialUiState.tasks.first().isDone)

        viewModel.toggleTask(task)
        advanceUntilIdle()

        val finalUiState = viewModel.uiState.value
        assertTrue(finalUiState.tasks.first().isDone)
        assertNull(finalUiState.errorMessage)
    }

    @Test
    fun `deleteTask removes a task`() = runTest {
        val task1 = Task("1", "Task 1", false)
        val task2 = Task("2", "Task 2", true)
        fakeTaskRepository.seed(listOf(task1, task2))

        // Re-initialize ViewModel to pick up seeded tasks
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.tasks.size)

        viewModel.deleteTask(task1)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.tasks.size)
        assertEquals(task2, uiState.tasks.first())
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `empty state is shown when no tasks are present`() = runTest {
        fakeTaskRepository.clear() // Ensure no tasks
        // Re-initialize ViewModel to pick up empty state
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.tasks.isEmpty())
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `initial loading error sets errorMessage`() = runTest {
        fakeTaskRepository.shouldThrowErrorOnObserveTasks = true

        // Re-initialize ViewModel to trigger error
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.errorMessage)
        assertEquals("Failed to load tasks", uiState.errorMessage)
        assertTrue(uiState.tasks.isEmpty())
    }

    @Test
    fun `addTask error sets errorMessage`() = runTest {
        fakeTaskRepository.shouldThrowErrorOnAddTask = true
        viewModel.onTaskTitleChange("Error Task")
        viewModel.addTask()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorMessage)
        assertEquals("Failed to add task", uiState.errorMessage)
        assertTrue(uiState.tasks.isEmpty()) // No task should be added
    }

    @Test
    fun `toggleTask error sets errorMessage`() = runTest {
        val task = Task("1", "Test Task", false)
        fakeTaskRepository.seed(listOf(task))

        // Re-initialize ViewModel to pick up seeded tasks
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        advanceUntilIdle()

        fakeTaskRepository.shouldThrowErrorOnToggleTask = true
        viewModel.toggleTask(task)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorMessage)
        assertEquals("Failed to toggle task", uiState.errorMessage)
        assertFalse(uiState.tasks.first().isDone) // Task state should not change
    }

    @Test
    fun `deleteTask error sets errorMessage`() = runTest {
        val task = Task("1", "Test Task", false)
        fakeTaskRepository.seed(listOf(task))

        // Re-initialize ViewModel to pick up seeded tasks
        viewModel =
            TodoViewModel(
                observeTasks = observeTasksUseCase,
                addTask = addTaskUseCase,
                toggleTask = toggleTaskUseCase,
                deleteTask = deleteTaskUseCase,
            )
        advanceUntilIdle()

        fakeTaskRepository.shouldThrowErrorOnDeleteTask = true
        viewModel.deleteTask(task)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNotNull(uiState.errorMessage)
        assertEquals("Failed to delete task", uiState.errorMessage)
        assertEquals(1, uiState.tasks.size) // Task should not be deleted
    }
}
