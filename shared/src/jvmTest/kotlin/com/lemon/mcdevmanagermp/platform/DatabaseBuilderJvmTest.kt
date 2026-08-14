package com.lemon.mcdevmanagermp.platform

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseBuilderJvmTest {

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
