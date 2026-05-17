package com.gai.gaiapp.domain.usecase

import com.gai.gaiapp.FakeTaskRepository
import com.gai.gaiapp.domain.model.Task
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UseCasesTest {

    private lateinit var fakeTaskRepository: FakeTaskRepository
    private lateinit var addTaskUseCase: AddTaskUseCase
    private lateinit var toggleTaskUseCase: ToggleTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private var uuidCounter = 0
    private val fakeUuidGenerator: () -> String = { "test-uuid-${uuidCounter++}" }

    @BeforeTest
    fun setup() {
        fakeTaskRepository = FakeTaskRepository()
        uuidCounter = 0 // Reset counter for each test
        addTaskUseCase = AddTaskUseCase(fakeTaskRepository, fakeUuidGenerator)
        toggleTaskUseCase = ToggleTaskUseCase(fakeTaskRepository)
        deleteTaskUseCase = DeleteTaskUseCase(fakeTaskRepository)
    }

    @Test
    fun `AddTaskUseCase adds a task with generated UUID`() = runTest {
        val title = "New Task"
        addTaskUseCase(title)

        val tasks = fakeTaskRepository.getTasks().first()
        assertEquals(1, tasks.size)
        val addedTask = tasks.first()
        assertEquals("test-uuid-0", addedTask.id)
        assertEquals(title, addedTask.title)
        assertFalse(addedTask.isDone)
    }

    @Test
    fun `AddTaskUseCase does not add a blank task`() = runTest {
        addTaskUseCase("   ")

        val tasks = fakeTaskRepository.getTasks().first()
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `ToggleTaskUseCase toggles task completion`() = runTest {
        val initialTask = Task("1", "Test Task", false)
        fakeTaskRepository.seed(listOf(initialTask))

        toggleTaskUseCase(initialTask)

        val tasks = fakeTaskRepository.getTasks().first()
        assertEquals(1, tasks.size)
        assertTrue(tasks.first().isDone)
    }

    @Test
    fun `ToggleTaskUseCase on non-existent task does nothing`() = runTest {
        val nonExistentTask = Task("99", "Non-existent", false)
        fakeTaskRepository.seed(listOf(Task("1", "Existing", false)))

        toggleTaskUseCase(nonExistentTask)

        val tasks = fakeTaskRepository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("1", tasks.first().id)
        assertFalse(tasks.first().isDone) // Should not have changed
    }

    @Test
    fun `DeleteTaskUseCase deletes a task`() = runTest {
        val task1 = Task("1", "Task 1", false)
        val task2 = Task("2", "Task 2", true)
        fakeTaskRepository.seed(listOf(task1, task2))

        deleteTaskUseCase(task1)

        val tasks = fakeTaskRepository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(task2, tasks.first())
    }

    @Test
    fun `DeleteTaskUseCase on non-existent task does nothing`() = runTest {
        val task1 = Task("1", "Task 1", false)
        fakeTaskRepository.seed(listOf(task1))
        val nonExistentTask = Task("99", "Non-existent", false)

        deleteTaskUseCase(nonExistentTask)

        val tasks = fakeTaskRepository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(task1, tasks.first())
    }
}
