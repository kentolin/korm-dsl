// examples/example-multiplatform/src/commonTest/kotlin/com/korm/examples/multiplatform/TaskRepositoryTest.kt

package com.korm.examples.multiplatform

import com.korm.dsl.core.Database
import com.korm.examples.multiplatform.models.*
import com.korm.examples.multiplatform.repositories.TaskRepository
import com.korm.examples.multiplatform.services.DatabaseInitializer
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.days

/**
 * Common tests for TaskRepository.
 * These tests run on all platforms (Android, iOS, JVM).
 */
class TaskRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: TaskRepository

    @BeforeTest
    fun setup() {
        // Create in-memory database for testing
        database = Database.connect(
            url = "jdbc:sqlite::memory:",
            driver = "org.sqlite.JDBC"
        )

        DatabaseInitializer.initialize(database)
        repository = TaskRepository(database)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCreateTask() = runTest {
        val task = repository.createTask(
            title = "Test Task",
            description = "Test Description",
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        assertNotNull(task)
        assertTrue(task.id > 0)
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(TaskPriority.HIGH, task.priority)
        assertEquals(TaskStatus.TODO, task.status)
    }

    @Test
    fun testFindById() = runTest {
        val created = repository.createTask(
            title = "Find Test",
            description = null,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.TODO
        )

        val found = repository.findById(created.id)

        assertNotNull(found)
        assertEquals(created.id, found.id)
        assertEquals(created.title, found.title)
    }

    @Test
    fun testFindAll() = runTest {
        repository.createTask("Task 1", null, TaskPriority.LOW, TaskStatus.TODO)
        repository.createTask("Task 2", null, TaskPriority.MEDIUM, TaskStatus.TODO)
        repository.createTask("Task 3", null, TaskPriority.HIGH, TaskStatus.TODO)

        val tasks = repository.findAll()

        assertTrue(tasks.size >= 3)
    }

    @Test
    fun testFindByStatus() = runTest {
        repository.createTask("Done Task", null, TaskPriority.LOW, TaskStatus.DONE)
        repository.createTask("TODO Task", null, TaskPriority.LOW, TaskStatus.TODO)

        val doneTasks = repository.findByStatus(TaskStatus.DONE)
        val todoTasks = repository.findByStatus(TaskStatus.TODO)

        assertTrue(doneTasks.any { it.title == "Done Task" })
        assertTrue(todoTasks.any { it.title == "TODO Task" })
    }

    @Test
    fun testFindByPriority() = runTest {
        repository.createTask("High Priority", null, TaskPriority.HIGH, TaskStatus.TODO)
        repository.createTask("Low Priority", null, TaskPriority.LOW, TaskStatus.TODO)

        val highPriorityTasks = repository.findByPriority(TaskPriority.HIGH)

        assertTrue(highPriorityTasks.any { it.title == "High Priority" })
        assertFalse(highPriorityTasks.any { it.title == "Low Priority" })
    }

    @Test
    fun testSearchByTitle() = runTest {
        repository.createTask("KORM Documentation", null, TaskPriority.HIGH, TaskStatus.TODO)
        repository.createTask("Review PRs", null, TaskPriority.MEDIUM, TaskStatus.TODO)

        val searchResults = repository.searchByTitle("KORM")

        assertTrue(searchResults.any { it.title == "KORM Documentation" })
        assertFalse(searchResults.any { it.title == "Review PRs" })
    }

    @Test
    fun testUpdateTask() = runTest {
        val task = repository.createTask(
            title = "Original Title",
            description = null,
            priority = TaskPriority.LOW,
            status = TaskStatus.TODO
        )

        val updated = repository.updateTask(
            id = task.id,
            title = "Updated Title",
            priority = TaskPriority.HIGH
        )

        assertEquals("Updated Title", updated.title)
        assertEquals(TaskPriority.HIGH, updated.priority)
    }

    @Test
    fun testUpdateStatus() = runTest {
        val task = repository.createTask(
            title = "Status Test",
            description = null,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.TODO
        )

        val updated = repository.updateStatus(task.id, TaskStatus.DONE)

        assertEquals(TaskStatus.DONE, updated.status)
    }

    @Test
    fun testDeleteTask() = runTest {
        val task = repository.createTask(
            title = "Delete Me",
            description = null,
            priority = TaskPriority.LOW,
            status = TaskStatus.TODO
        )

        val deleted = repository.deleteTask(task.id)
        assertTrue(deleted)

        val found = repository.findById(task.id)
        assertNull(found)
    }

    @Test
    fun testGetStatistics() = runTest {
        repository.createTask("Task 1", null, TaskPriority.LOW, TaskStatus.TODO)
        repository.createTask("Task 2", null, TaskPriority.LOW, TaskStatus.IN_PROGRESS)
        repository.createTask("Task 3", null, TaskPriority.LOW, TaskStatus.DONE)

        val stats = repository.getStatistics()

        assertTrue(stats.total >= 3)
        assertTrue(stats.todo >= 1)
        assertTrue(stats.inProgress >= 1)
        assertTrue(stats.done >= 1)
    }

    @Test
    fun testOverdueTasks() = runTest {
        val pastDate = Clock.System.now().minus(5.days)
        val futureDate = Clock.System.now().plus(5.days)

        repository.createTask(
            title = "Overdue Task",
            description = null,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO,
            dueDate = pastDate
        )

        repository.createTask(
            title = "Future Task",
            description = null,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.TODO,
            dueDate = futureDate
        )

        val overdueTasks = repository.findOverdue()

        assertTrue(overdueTasks.any { it.title == "Overdue Task" })
        assertFalse(overdueTasks.any { it.title == "Future Task" })
    }
}
