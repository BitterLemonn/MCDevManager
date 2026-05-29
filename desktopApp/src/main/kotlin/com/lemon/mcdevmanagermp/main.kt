package com.lemon.mcdevmanagermp

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.lemon.mcdevmanagermp.utils.CrashHandler
import java.util.prefs.Preferences

private const val KEY_X = "window_x"
private const val KEY_Y = "window_y"
private const val KEY_WIDTH = "window_width"
private const val KEY_HEIGHT = "window_height"
private const val KEY_MAXIMIZED = "window_maximized"

private val prefs = Preferences.userRoot().node("mcdevmanagermpr/window")

private fun loadWindowState(): Triple<DpSize, WindowPosition, WindowPlacement> {
    val width = prefs.getInt(KEY_WIDTH, 1200)
    val height = prefs.getInt(KEY_HEIGHT, 750)
    val x = prefs.getInt(KEY_X, -1)
    val y = prefs.getInt(KEY_Y, -1)
    val maximized = prefs.getBoolean(KEY_MAXIMIZED, false)

    val size = DpSize(width.dp, height.dp)
    val position = if (x >= 0 && y >= 0) WindowPosition(x.dp, y.dp) else WindowPosition.PlatformDefault
    val placement = if (maximized) WindowPlacement.Maximized else WindowPlacement.Floating
    return Triple(size, position, placement)
}

private fun saveWindowState(state: androidx.compose.ui.window.WindowState) {
    prefs.putBoolean(KEY_MAXIMIZED, state.placement == WindowPlacement.Maximized)
    if (state.placement == WindowPlacement.Floating) {
        prefs.putInt(KEY_WIDTH, state.size.width.value.toInt())
        prefs.putInt(KEY_HEIGHT, state.size.height.value.toInt())
        val pos = state.position
        if (pos.isSpecified) {
            prefs.putInt(KEY_X, pos.x.value.toInt())
            prefs.putInt(KEY_Y, pos.y.value.toInt())
        }
    }
}

fun main() {
    CrashHandler.init()

    application {
        val (size, position, placement) = loadWindowState()
        val windowState = rememberWindowState(
            placement = placement,
            position = position,
            size = size
        )

        Window(
            onCloseRequest = {
                saveWindowState(windowState)
                exitApplication()
            },
            state = windowState,
            title = "MCDevManagerMPR",
        ) {
            App()
        }
    }
}
