package com.lemon.mcdevmanagermp.data.db

import androidx.room.Room
import androidx.room.RoomDatabaseConstructor
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual object AppDatabaseConstructor :
    RoomDatabaseConstructor<AppDatabase> {
    actual override fun initialize(): AppDatabase {
        return createDatabase()
    }
}

private fun databasePath(): String {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomains = NSUserDomainMask
    )
    val appSupportUrl = urls.first() as NSURL

    return appSupportUrl.path + "/${DATABASE_NAME}"
}

fun createDatabase(): AppDatabase {
    val dbFile = databasePath()

    return Room.databaseBuilder<AppDatabase>(name = dbFile)
        .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .build()
}