package com.lemon.mcdevmanagermp.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
import com.lemon.mcdevmanagermp.data.db.AppDatabase
import com.lemon.mcdevmanagermp.data.db.AppDatabaseConstructor
import com.lemon.mcdevmanagermp.data.db.MIGRATION_1_2
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val documentDirectory = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true
    ).first() as String
    val dbFilePath = "$documentDirectory/$DATABASE_NAME"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2)
}
