package com.lemon.mcdevmanagermp.platform

import androidx.room.RoomDatabase
import com.lemon.mcdevmanagermp.data.db.AppDatabase

expect fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
