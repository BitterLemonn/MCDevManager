package com.lemon.mcdevmanagermp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.lemon.mcdevmanagermp.platform.AndroidLogContext
import com.lemon.mcdevmanagermp.platform.ThemeRepository
import com.lemon.mcdevmanagermp.platform.UpdatePreferences
import com.lemon.mcdevmanagermp.utils.CrashHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 初始化 Android 上下文（日志目录需要）
        AndroidLogContext.setContext(applicationContext)

        // 初始化主题持久化
        ThemeRepository.init(applicationContext)

        // 初始化更新偏好持久化
        UpdatePreferences.init(applicationContext)

        // 注册全局异常捕获
        CrashHandler.init()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}