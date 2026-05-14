package com.gainus.gaiapp

import com.gainus.gaiapp.database.TaskEntity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RoomTaskRepositoryTest {

    private lateinit var fakeTaskDao: FakeTaskDao
    private lateinit var repository: RoomTaskRepository

    @BeforeTest
    fun setup() {
        fakeTaskDao = FakeTaskDao()
        repository = RoomTaskRepository(fakeTaskDao)
    }

    @Test
    fun `addTask inserts a new task`() = runTest {
        val task = Task("1", "Test Task", false)
        repository.addTask(task)

        val tasks = repository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(task, tasks.first())
    }

    @Test
    fun `toggleTask changes isDone status`() = runTest {
        val initialTask = Task("1", "Test Task", false)
        repository.addTask(initialTask)

        val tasksBeforeToggle = repository.getTasks().first()
        assertFalse(tasksBeforeToggle.first().isDone)

        repository.toggleTask(initialTask)

        val tasksAfterToggle = repository.getTasks().first()
        assertTrue(tasksAfterToggle.first().isDone)
    }

    @Test
    fun `deleteTask removes a task`() = runTest {
        val task1 = Task("1", "Task 1", false)
        val task2 = Task("2", "Task 2", true)
        repository.addTask(task1)
        repository.addTask(task2)

        var tasks = repository.getTasks().first()
        assertEquals(2, tasks.size)

        repository.deleteTask(task1)

        tasks = repository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(task2, tasks.first())
    }

    @Test
    fun `getTasks observes all tasks`() = runTest {
        val task1 = TaskEntity("1", "Task 1", false)
        val task2 = TaskEntity("2", "Task 2", true)
        fakeTaskDao.seed(listOf(task1, task2))

        val tasks = repository.getTasks().first()
        assertEquals(2, tasks.size)
        assertTrue(tasks.any { it.id == "1" && it.title == "Task 1" && !it.isDone })
        assertTrue(tasks.any { it.id == "2" && it.title == "Task 2" && it.isDone })
    }

    @Test
    fun `addTask with existing id replaces the task`() = runTest {
        val task1 = Task("1", "Original Task", false)
        repository.addTask(task1)

        val updatedTask = Task("1", "Updated Task", true)
        repository.addTask(updatedTask) // insertTask uses OnConflictStrategy.REPLACE

        val tasks = repository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(updatedTask, tasks.first())
    }

    @Test
    fun `toggleTask on non-existent task does nothing`() = runTest {
        val task = Task("1", "Non-existent", false)
        repository.toggleTask(task)

        val tasks = repository.getTasks().first()
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun `deleteTask on non-existent task does nothing`() = runTest {
        val task1 = Task("1", "Task 1", false)
        repository.addTask(task1)

        val nonExistentTask = Task("2", "Non-existent", false)
        repository.deleteTask(nonExistentTask)

        val tasks = repository.getTasks().first()
        assertEquals(1, tasks.size)
        assertEquals(task1, tasks.first())
    }
}
