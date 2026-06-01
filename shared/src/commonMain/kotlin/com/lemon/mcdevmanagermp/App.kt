package com.lemon.mcdevmanagermp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lemon.mcdevmanagermp.ui.navigation.AppNavigation
import com.lemon.mcdevmanagermp.ui.theme.AppTheme
import com.lemon.mcdevmanagermp.ui.theme.LocalThemeViewModel
import com.lemon.mcdevmanagermp.ui.theme.ThemeViewModel

@Composable
@Preview
fun App() {
    val viewModel = remember { ThemeViewModel() }
    CompositionLocalProvider(LocalThemeViewModel provides viewModel) {
        AppTheme(viewModel = viewModel) {
            Surface(modifier = Modifier.fillMaxSize()) {
                AppNavigation()
            }
        }
    }
}
