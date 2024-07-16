package com.lemon.mcdevmanager.data.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.lemon.mcdevmanager.data.database.dao.InfoDao
import com.lemon.mcdevmanager.data.database.dao.UserDao
import com.lemon.mcdevmanager.data.database.entities.AnalyzeEntity
import com.lemon.mcdevmanager.data.database.entities.OverviewEntity
import com.lemon.mcdevmanager.data.database.entities.UserEntity

@Database(
    entities = [UserEntity::class, OverviewEntity::class, AnalyzeEntity::class],
    version = 2
)
abstract class AppDataBase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        private const val DATABASE_NAME = "mcDevManager.db"

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    AppDataBase::class.java,
                    DATABASE_NAME
                ).addMigrations(Migration(1, 2) {
                    it.execSQL("CREATE TABLE IF NOT EXISTS `analyzeEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nickname` TEXT NOT NULL, `filterType` INTEGER NOT NULL, `platform` TEXT NOT NULL, `startDate` TEXT NOT NULL, `endDate` TEXT NOT NULL, `filterResourceList` TEXT NOT NULL, `createTime` INTEGER NOT NULL)")
                }).build().also { instance = it }
            }
        }
    }

    abstract fun userDao(): UserDao

    abstract fun infoDao(): InfoDao

}