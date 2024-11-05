package com.lemon.mcdevmanager

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.base.BaseScaffold
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.lemon.mcdevmanager.ui.theme.Purple200
import com.lemon.mcdevmanager.ui.theme.Purple40
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val formatStrategy: FormatStrategy =
            PrettyFormatStrategy.newBuilder().showThreadInfo(true).methodCount(4).tag("MCDevLogger")
                .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        val fileName = SimpleDateFormat(
            "yyyy_MM_dd-HH:mm:ss",
            Locale.CHINA
        ).format(System.currentTimeMillis()) + ".log"
        val logDirPath =
            this.getExternalFilesDir("logger" + File.separatorChar + "mcDevMng")?.absolutePath ?: ""
        AppContext.logDirPath = logDirPath
        Logger.addLogAdapter(DiskLogAdapter(fileName, logDirPath))

        resetStatusBarStyle()
        super.onCreate(savedInstanceState)
        setContent {
            MCDevManagerTheme {
                BaseScaffold()
            }
        }
    }

    private fun isNightMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun overrideStatusBarStyle(lightColor: Int, darkColor: Int) {
        if (isNightMode(this)) {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(darkColor))
        } else {
            enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(lightColor, darkColor))
        }
    }

    fun resetStatusBarStyle() {
        if (isNightMode(this)) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Purple40.toArgb()),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                )
            )
        } else {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Purple200.toArgb(), Purple40.toArgb()),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                )
            )
        }
    }
}