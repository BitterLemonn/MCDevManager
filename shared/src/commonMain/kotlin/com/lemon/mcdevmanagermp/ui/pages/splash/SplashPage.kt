package com.lemon.mcdevmanagermp.ui.pages.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanagermp.ui.components.AppScaffold
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors
import androidx.compose.runtime.collectAsState

@Composable
fun SplashPage(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val viewModel = remember { SplashViewModel() }
    val state by viewModel.state.collectAsState()

    AppScaffold(
        viewEffect = viewModel.effect,
        onEffect = { effect ->
            when (effect) {
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
                SplashEffect.NavigateToMain -> onNavigateToMain()
            }
        }
    ) { _ ->
        val colors = LocalAppColors.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "MCDevManager",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.onPrimaryContainer
                )
                AnimatedVisibility(visible = state.isLoading, enter = fadeIn()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}
