package com.lemon.mcdevmanagermp.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
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
}
