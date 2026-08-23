package com.lemon.mcdevmanagermp.platform

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DatabaseBuilderJvmTest {

    @Test
    fun `hot reload stores data below the stable Gradle build root`() {
        val buildRoot = File("/project")

        assertEquals(
            buildRoot.absoluteFile,
            resolveHotReloadApplicationDirectory(
                isActive = "true",
                buildRoot = buildRoot.path
            )
        )
    }

    @Test
    fun `normal launch ignores the Gradle build root`() {
        assertNull(
            resolveHotReloadApplicationDirectory(
                isActive = "false",
                buildRoot = "/project"
            )
        )
    }

    @Test
    fun `hot reload restores the newest database from the generated classpath`() {
        val tempDirectory = Files.createTempDirectory("hot-run-").toFile()
        val runDirectory = tempDirectory.resolve("run")
        val targetDirectory = tempDirectory.resolve("stable-data")
        val oldDataDirectory = runDirectory.resolve("classpath/libs/shared/old/.data")
        val newDataDirectory = runDirectory.resolve("classpath/libs/shared/new/.data")
        oldDataDirectory.mkdirs()
        newDataDirectory.mkdirs()
        oldDataDirectory.resolve("mc_dev_manager.db").apply {
            writeText("old")
            setLastModified(1L)
        }
        newDataDirectory.resolve("mc_dev_manager.db").apply {
            writeText("new")
            setLastModified(2L)
        }
        newDataDirectory.resolve("mc_dev_manager.db-wal").writeText("wal")

        try {
            migrateLegacyHotReloadDatabase(targetDirectory, runDirectory)

            assertEquals("new", targetDirectory.resolve("mc_dev_manager.db").readText())
            assertEquals("wal", targetDirectory.resolve("mc_dev_manager.db-wal").readText())
        } finally {
            tempDirectory.deleteRecursively()
        }
    }

    @Test
    fun `portable distribution keeps data next to the application when writable`() {
        val applicationDirectory = File("/portable/app")
        val userDataDirectory = File("/user-data/MCDevManager")

        val directory = selectJvmDatabaseDirectory(
            applicationDirectory = applicationDirectory,
            userDataDirectory = userDataDirectory,
            prepareDirectory = { it == applicationDirectory.resolve(".data") }
        )

        assertEquals(applicationDirectory.resolve(".data").absoluteFile, directory)
    }

    @Test
    fun `installed distribution falls back to user data when application directory is read-only`() {
        val applicationDirectory = File("/program-files/MCDevManager/app")
        val userDataDirectory = File("/user-data/MCDevManager")

        val directory = selectJvmDatabaseDirectory(
            applicationDirectory = applicationDirectory,
            userDataDirectory = userDataDirectory,
            prepareDirectory = { it == userDataDirectory }
        )

        assertEquals(userDataDirectory.absoluteFile, directory)
    }

    @Test
    fun `windows stores data below LocalAppData`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Windows 11",
            userHome = "/users/test",
            environment = mapOf("LOCALAPPDATA" to "/local-app-data")
        )

        assertEquals(File("/local-app-data/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `windows falls back to the user profile`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Windows 11",
            userHome = "/users/test",
            environment = emptyMap()
        )

        assertEquals(File("/users/test/AppData/Local/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `macOS stores data below Application Support`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Mac OS X",
            userHome = "/users/test",
            environment = emptyMap()
        )

        assertEquals(
            File("/users/test/Library/Application Support/MCDevManager").absoluteFile,
            directory
        )
    }

    @Test
    fun `linux honors XDG data home`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Linux",
            userHome = "/home/test",
            environment = mapOf("XDG_DATA_HOME" to "/xdg-data")
        )

        assertEquals(File("/xdg-data/MCDevManager").absoluteFile, directory)
    }

    @Test
    fun `linux falls back to the user data directory`() {
        val directory = resolveJvmAppDataDirectory(
            osName = "Linux",
            userHome = "/home/test",
            environment = emptyMap()
        )

        assertEquals(File("/home/test/.local/share/MCDevManager").absoluteFile, directory)
    }
}
