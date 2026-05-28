package com.lemon.mcdevmanagermp.utils

import com.lemon.mcdevmanagermp.platform.setupUncaughtExceptionHandler

object CrashHandler {

    fun init() {
        setupUncaughtExceptionHandler()
        Logger.i("全局异常捕获器已初始化")
    }

    internal fun handleException(throwable: Throwable) {
        Logger.e("未捕获的异常:\n${throwable.stackTraceToString()}")
    }
}
