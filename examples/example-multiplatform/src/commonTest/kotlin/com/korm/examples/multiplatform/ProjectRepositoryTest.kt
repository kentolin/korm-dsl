// examples/example-multiplatform/src/commonTest/kotlin/com/korm/examples/multiplatform/ProjectRepositoryTest.kt

package com.korm.examples.multiplatform

import com.korm.dsl.core.Database
import com.korm.examples.multiplatform.repositories.ProjectRepository
import com.korm.examples.multiplatform.services.DatabaseInitializer
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Common tests for ProjectRepository.
 */
class ProjectRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: ProjectRepository

    @BeforeTest
    fun setup() {
        database = Database.connect(
            url = "jdbc:sqlite::memory:",
            driver = "org.sqlite.JDBC"
        )

        DatabaseInitializer.initialize(database)
        repository = ProjectRepository(database)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCreateProject() = runTest {
        val project = repository.createProject(
            name = "Test Project",
            description = "Test Description",
            color = "#FF5722"
        )

        assertNotNull(project)
        assertTrue(project.id > 0)
        assertEquals("Test Project", project.name)
        assertEquals("Test Description", project.description)
        assertEquals("#FF5722", project.color)
        assertFalse(project.isArchived)
    }

    @Test
    fun testFindAll() = runTest {
        repository.createProject("Project 1", null, "#FF0000")
        repository.createProject("Project 2", null, "#00FF00")

        val projects = repository.findAll()

        assertTrue(projects.size >= 2)
    }

    @Test
    fun testArchiveProject() = runTest {
        val project = repository.createProject("Archive Test", null)

        val archived = repository.archiveProject(project.id)

        assertTrue(archived.isArchived)
    }

    @Test
    fun testUnarchiveProject() = runTest {
        val project = repository.createProject("Unarchive Test", null)
        repository.archiveProject(project.id)

        val unarchived = repository.unarchiveProject(project.id)

        assertFalse(unarchived.isArchived)
    }

    @Test
    fun testDeleteProject() = runTest {
        val project = repository.createProject("Delete Test", null)

        val deleted = repository.deleteProject(project.id)
        assertTrue(deleted)

        val found = repository.findById(project.id)
        assertNull(found)
    }
}
