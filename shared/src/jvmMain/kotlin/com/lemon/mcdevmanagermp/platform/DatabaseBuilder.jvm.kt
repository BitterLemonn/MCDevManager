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
import java.io.File

private object DbClassRef

actual fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val jarPath = DbClassRef::class.java.protectionDomain.codeSource.location.toURI().path
    val dataDir = File(File(jarPath).parentFile, ".data")
    if (!dataDir.exists()) dataDir.mkdirs()
    val dbFile = File(dataDir, DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
