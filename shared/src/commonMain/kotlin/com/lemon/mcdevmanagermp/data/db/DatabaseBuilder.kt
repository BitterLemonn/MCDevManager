package com.lemon.mcdevmanagermp.data.db

import androidx.room.RoomDatabase

expect fun createAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
