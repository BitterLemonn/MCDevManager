package com.lemon.mcdevmanagermp.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lemon.mcdevmanagermp.data.consts.DATABASE_NAME
import com.lemon.mcdevmanagermp.data.db.AppDatabase
import com.lemon.mcdevmanagermp.data.db.MIGRATION_1_2
import com.lemon.mcdevmanagermp.data.db.MIGRATION_2_3
import com.lemon.mcdevmanagermp.data.db.MIGRATION_3_4
import com.lemon.mcdevmanagermp.data.db.MIGRATION_4_5

actual fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidLogContext.getContext()
        ?: throw IllegalStateException("请先调用 AndroidLogContext.setContext(context)")
    val dbFile = context.getDatabasePath(DATABASE_NAME)
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
