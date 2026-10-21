package com.lemon.mcdevmanagermp.platform

import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun getPasswordKeyDirectory(): String {
    val baseDirectory = NSSearchPathForDirectoriesInDomains(
        NSApplicationSupportDirectory, NSUserDomainMask, true
    ).first() as String
    return "$baseDirectory/$PASSWORD_KEY_DIR_NAME"
}

private const val PASSWORD_KEY_DIR_NAME = "password_keys"
