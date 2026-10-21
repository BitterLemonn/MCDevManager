package com.lemon.mcdevmanagermp.platform

import android.content.Context

actual fun getPasswordKeyDirectory(): String {
    val context = AndroidLogContext.getContext()
        ?: throw IllegalStateException("请先调用 AndroidLogContext.setContext(context)")
    return context.getDir(PASSWORD_KEY_DIR_NAME, Context.MODE_PRIVATE).absolutePath
}

private const val PASSWORD_KEY_DIR_NAME = "password_keys"
