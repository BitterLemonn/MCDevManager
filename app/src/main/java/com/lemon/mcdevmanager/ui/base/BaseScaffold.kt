package com.lemon.mcdevmanager.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lemon.mcdevmanager.data.common.FEEDBACK_PAGE
import com.lemon.mcdevmanager.data.common.LOGIN_PAGE
import com.lemon.mcdevmanager.data.common.MAIN_PAGE
import com.lemon.mcdevmanager.data.common.SPLASH_PAGE
import com.lemon.mcdevmanager.ui.page.FeedbackPage
import com.lemon.mcdevmanager.ui.page.LoginPage
import com.lemon.mcdevmanager.ui.page.MainPage
import com.lemon.mcdevmanager.ui.page.SplashPage
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.widget.AppSnackbar
import com.lemon.mcdevmanager.ui.widget.popupSnackBar

@Composable
fun BaseScaffold() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    fun showToast(msg: String, flag: String) =
        popupSnackBar(
            scope = scope,
            snackbarHostState = snackbarHostState,
            message = msg,
            label = flag
        )


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { AppSnackbar(data = it) }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            modifier = Modifier
                .background(color = AppTheme.colors.background)
                .fillMaxSize()
                .padding(padding),
            startDestination = SPLASH_PAGE
        ) {
            composable(route = SPLASH_PAGE) {
                SplashPage(navController = navController)
            }
            composable(route = LOGIN_PAGE) {
                LoginPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            composable(route = MAIN_PAGE) {
                MainPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
            composable(route = FEEDBACK_PAGE) {
                FeedbackPage(
                    navController = navController,
                    showToast = { msg, flag -> showToast(msg, flag) }
                )
            }
        }
    }
}