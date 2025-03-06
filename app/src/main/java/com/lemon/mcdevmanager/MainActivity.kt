package com.lemon.mcdevmanager

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
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
        // 初始化日志
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

        // 允许在状态栏渲染内容
        WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge(
            // 透明状态栏
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            // 透明导航栏
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
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