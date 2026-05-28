package com.lemon.mcdevmanagermp.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lemon.mcdevmanagermp.ui.pages.login.LoginPage
import com.lemon.mcdevmanagermp.ui.pages.main.MainPage
import com.lemon.mcdevmanagermp.ui.pages.splash.SplashPage

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = remember { mutableStateListOf<Route>(Route.Splash) }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = backStack.last(),
            transitionSpec = {
                if (initialState is Route.Splash) {
                    fadeIn(animationSpec = tween(500)) togetherWith
                            fadeOut(animationSpec = tween(500))
                } else {
                    slideInHorizontally(
                        animationSpec = tween(350),
                        initialOffsetX = { it / 2 }
                    ) + fadeIn(animationSpec = tween(350)) togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(350),
                                targetOffsetX = { -it / 2 }
                            ) + fadeOut(animationSpec = tween(350))
                }
            },
            label = "nav_transition"
        ) { route ->
            Box(Modifier.fillMaxSize()) {
                when (route) {
                    is Route.Splash -> SplashPage(
                        onNavigateToMain = { backStack.add(Route.Login) }
                    )

                    is Route.Login -> LoginPage(
                        onNavigateToMain = {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    )

                    is Route.Main -> MainPage()
                }
            }
        }
    }
}
