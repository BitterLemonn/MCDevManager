package com.lemon.mcdevmanagermp.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
import com.lemon.mcdevmanagermp.data.db.AppDatabase
import com.lemon.mcdevmanagermp.data.db.AppDatabaseConstructor
import com.lemon.mcdevmanagermp.data.db.MIGRATION_1_2
import com.lemon.mcdevmanagermp.data.db.MIGRATION_2_3
import com.lemon.mcdevmanagermp.data.db.MIGRATION_3_4
import com.lemon.mcdevmanagermp.data.db.MIGRATION_4_5
import com.lemon.mcdevmanagermp.data.db.MIGRATION_5_6
import com.lemon.mcdevmanagermp.data.db.MIGRATION_6_7
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private const val APP_DATA_DIRECTORY_NAME = "MCDevManager"
private const val HOT_RELOAD_ACTIVE_PROPERTY = "compose.reload.isActive"
private const val GRADLE_BUILD_ROOT_PROPERTY = "gradle.build.root"
private const val HOT_RELOAD_ARGFILE_PROPERTY = "compose.reload.argfile"
private object DbClassRef

actual fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dataDir = resolveJvmDatabaseDirectory()
    val dbFile = File(dataDir, DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
}

private fun resolveJvmDatabaseDirectory(): File {
    val codeLocation = File(DbClassRef::class.java.protectionDomain.codeSource.location.toURI())
    val hotReloadApplicationDirectory = resolveHotReloadApplicationDirectory()
    val applicationDirectory = hotReloadApplicationDirectory ?: codeLocation.parentFile
        ?: error("Unable to determine the application directory")
    val dataDirectory = selectJvmDatabaseDirectory(applicationDirectory, resolveJvmAppDataDirectory())
    if (hotReloadApplicationDirectory != null) {
        migrateLegacyHotReloadDatabase(dataDirectory, resolveHotReloadRunDirectory())
    }
    return dataDirectory
}

internal fun resolveHotReloadApplicationDirectory(
    isActive: String? = System.getProperty(HOT_RELOAD_ACTIVE_PROPERTY),
    buildRoot: String? = System.getProperty(GRADLE_BUILD_ROOT_PROPERTY)
): File? {
    if (!isActive.toBoolean() || buildRoot.isNullOrBlank()) return null
    return File(buildRoot).absoluteFile
}

internal fun resolveHotReloadRunDirectory(
    argFile: String? = System.getProperty(HOT_RELOAD_ARGFILE_PROPERTY)
): File? = argFile?.takeIf(String::isNotBlank)?.let(::File)?.absoluteFile?.parentFile

internal fun migrateLegacyHotReloadDatabase(targetDirectory: File, runDirectory: File?) {
    if (runDirectory == null || targetDirectory.resolve(DATABASE_NAME).exists()) return
    val sourceDatabase = runDirectory.walkTopDown()
        .maxDepth(8)
        .filter { it.isFile && it.name == DATABASE_NAME }
        .maxByOrNull(File::lastModified)
        ?: return

    Files.createDirectories(targetDirectory.toPath())
    sourceDatabase.parentFile.listFiles()
        ?.filter { it.name == DATABASE_NAME || it.name.startsWith("$DATABASE_NAME-") }
        ?.forEach { it.copyTo(targetDirectory.resolve(it.name), overwrite = false) }
}

/**
 * Keeps the existing directory-next-to-the-JAR behavior for writable portable distributions, but
 * falls back to per-user storage for installed applications (for example, below Program Files).
 */
internal fun selectJvmDatabaseDirectory(
    applicationDirectory: File,
    userDataDirectory: File,
    prepareDirectory: (File) -> Boolean = ::prepareWritableDirectory
): File {
    val portableDataDirectory = applicationDirectory.resolve(".data")
    if (prepareDirectory(portableDataDirectory)) return portableDataDirectory.absoluteFile
    check(prepareDirectory(userDataDirectory)) {
        "Unable to create a writable application data directory at ${userDataDirectory.absolutePath}"
    }
    return userDataDirectory.absoluteFile
}

private fun prepareWritableDirectory(directory: File): Boolean {
    var writeProbe: Path? = null
    return try {
        Files.createDirectories(directory.toPath())
        writeProbe = Files.createTempFile(directory.toPath(), ".mcdev-write-probe-", ".tmp")
        true
    } catch (_: IOException) {
        false
    } catch (_: SecurityException) {
        false
    } finally {
        writeProbe?.let { probe ->
            try {
                Files.deleteIfExists(probe)
            } catch (_: IOException) {
                // A failed probe cleanup must not make a writable database directory unusable.
            }
        }
    }
}

/**
 * Returns a per-user, writable application data directory.
 *
 * A packaged desktop application is normally installed below Program Files on Windows. That
 * directory is read-only for non-elevated users, so persistent data must not be stored next to
 * the application JAR.
 */
internal fun resolveJvmAppDataDirectory(
    osName: String = System.getProperty("os.name").orEmpty(),
    userHome: String = System.getProperty("user.home").orEmpty(),
    environment: Map<String, String> = System.getenv()
): File {
    val homeDirectory = userHome.takeIf(String::isNotBlank)?.let(::File)
    val baseDirectory = when {
        osName.startsWith("Windows", ignoreCase = true) -> {
            environment["LOCALAPPDATA"]
                ?.takeIf(String::isNotBlank)
                ?.let(::File)
                ?: homeDirectory?.resolve("AppData/Local")
        }

        osName.startsWith("Mac", ignoreCase = true) -> {
            homeDirectory?.resolve("Library/Application Support")
        }

        else -> {
            environment["XDG_DATA_HOME"]
                ?.takeIf(String::isNotBlank)
                ?.let(::File)
                ?: homeDirectory?.resolve(".local/share")
        }
    } ?: error("Unable to determine the current user's application data directory")

    return baseDirectory.resolve(APP_DATA_DIRECTORY_NAME).absoluteFile
}
