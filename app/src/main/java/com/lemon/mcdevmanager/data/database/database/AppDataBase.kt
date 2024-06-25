package com.lemon.mcdevmanager.data.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lemon.mcdevmanager.data.database.dao.UserDao
import com.lemon.mcdevmanager.data.database.entities.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1
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
                ).build().also { instance = it }
            }
        }
    }

    abstract fun userDao(): UserDao

}