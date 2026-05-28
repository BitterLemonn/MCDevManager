package com.lemon.mcdevmanagermp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.lemon.mcdevmanagermp.utils.CrashHandler

fun main() {
    CrashHandler.init()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MCDevManagerMPR",
        ) {
            App()
        }
    }
}