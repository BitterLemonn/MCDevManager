package com.lemon.mcdevmanagermp.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
import java.io.File

private object ClassRef

actual fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val jarPath = ClassRef::class.java.protectionDomain.codeSource.location.toURI().path
    val dataDir = File(File(jarPath).parentFile, ".data")
    if (!dataDir.exists()) dataDir.mkdirs()
    val dbFile = File(dataDir, DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(BundledSQLiteDriver())
}
