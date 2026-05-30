package com.lemon.mcdevmanagermp.platform

import android.content.Context
import com.lemon.mcdevmanagermp.utils.CrashHandler
import java.lang.ref.WeakReference

object AndroidLogContext {
    private var contextRef: WeakReference<Context>? = null

    fun setContext(context: Context) {
        contextRef = WeakReference(context)
    }

    fun getContext(): Context? = contextRef?.get()
}

actual fun getLogDirectory(): String {
    val context = AndroidLogContext.getContext()
        ?: throw IllegalStateException("安卓平台无法获取日志目录, 请先调用 AndroidLogContext.setContext(context) 初始化")
    val dir = context.getExternalFilesDir("logs") ?: context.filesDir
    return dir.absolutePath
}

actual fun setupUncaughtExceptionHandler() {
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        CrashHandler.handleException(throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}