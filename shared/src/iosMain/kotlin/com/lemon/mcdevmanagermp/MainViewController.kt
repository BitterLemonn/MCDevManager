package com.lemon.mcdevmanagermp

import androidx.compose.ui.window.ComposeUIViewController
import com.lemon.mcdevmanagermp.utils.CrashHandler

fun MainViewController() = ComposeUIViewController {
    CrashHandler.init()
    App()
}