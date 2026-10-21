package com.lemon.mcdevmanagermp.platform

import java.io.File

actual fun getPasswordKeyDirectory(): String =
    File(resolveJvmAppDataDirectory(), PASSWORD_KEY_DIR_NAME).absolutePath

private const val PASSWORD_KEY_DIR_NAME = "password_keys"
