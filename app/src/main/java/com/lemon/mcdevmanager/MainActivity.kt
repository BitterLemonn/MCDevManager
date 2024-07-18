package com.lemon.mcdevmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lemon.mcdevmanager.data.global.AppContext
import com.lemon.mcdevmanager.ui.base.BaseScaffold
import com.lemon.mcdevmanager.ui.theme.MCDevManagerTheme
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Logger.addLogAdapter(AndroidLogAdapter())
        val fileName = SimpleDateFormat("yyyy_MM_dd-HH:mm:ss", Locale.CHINA)
            .format(System.currentTimeMillis()) + ".log"
        val logDirPath =
            this.getExternalFilesDir("logger" + File.separatorChar + "mcDevMng")?.absolutePath ?: ""
        AppContext.logDirPath = logDirPath
        Logger.addLogAdapter(DiskLogAdapter(fileName, logDirPath))

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            MCDevManagerTheme {
                BaseScaffold()
            }
        }
    }
}