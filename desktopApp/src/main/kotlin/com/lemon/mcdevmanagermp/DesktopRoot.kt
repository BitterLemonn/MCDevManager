@file:OptIn(com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi::class)

package com.lemon.mcdevmanagermp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.lemon.mcdevmanagermp.ui.DesktopTitleBar
import com.lemon.mcdevmanagermp.ui.components.SketchImageLoader
import com.lemon.mcdevmanagermp.ui.navigation.AppNavigation
import com.lemon.mcdevmanagermp.ui.theme.AppTheme
import com.lemon.mcdevmanagermp.ui.theme.LocalThemeViewModel
import com.lemon.mcdevmanagermp.ui.theme.ThemeViewModel
import com.mohamedrejeb.richeditor.model.LocalImageLoader

private val WINDOW_CORNER = 10.dp

// ponytail: transparent=true 丢失 Windows 原生阴影；要阴影得调 DwmExtendFrameIntoClientArea，过重
@Composable
fun FrameWindowScope.DesktopRoot(
    windowState: WindowState,
    title: String,
    onClose: () -> Unit,
) {
    val viewModel = remember { ThemeViewModel() }
    val maximized = windowState.placement == WindowPlacement.Maximized
    val cornerRadius = if (maximized) 0.dp else WINDOW_CORNER

    CompositionLocalProvider(
        LocalThemeViewModel provides viewModel,
        LocalImageLoader provides SketchImageLoader,
    ) {
        AppTheme(viewModel = viewModel) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(cornerRadius),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    DesktopTitleBar(
                        title = title,
                        state = windowState,
                        onClose = onClose,
                    )
                    AppNavigation()
                }
            }
        }
    }
}
