package com.lemon.mcdevmanager.data.database.database

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class GlobalDataBase : Application() {
    companion object {
        lateinit var database: AppDataBase
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化数据库
        database = AppDataBase.getInstance(applicationContext)
    }

}