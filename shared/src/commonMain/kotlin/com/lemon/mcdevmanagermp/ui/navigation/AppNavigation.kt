package com.lemon.mcdevmanagermp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lemon.mcdevmanagermp.ui.components.LocalSnackbarHostState
import com.lemon.mcdevmanagermp.ui.components.LocalWindowWidthSizeClass
import com.lemon.mcdevmanagermp.ui.pages.income.IncomePage
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.IncomeDetailPage
import com.lemon.mcdevmanagermp.ui.pages.login.LoginPage
import com.lemon.mcdevmanagermp.ui.pages.mailbox.MailboxPage
import com.lemon.mcdevmanagermp.ui.pages.main.MainPage
import com.lemon.mcdevmanagermp.ui.pages.settings.SettingsContent
import com.lemon.mcdevmanagermp.ui.pages.splash.SplashPage
import com.lemon.mcdevmanagermp.ui.theme.LocalAppColors

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = LocalAppColors.current

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        BoxWithConstraints(modifier = modifier) {
            val widthSizeClass = when {
                maxWidth < 600.dp -> WindowWidthSizeClass.Compact
                maxWidth < 840.dp -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Expanded
            }
            CompositionLocalProvider(LocalWindowWidthSizeClass provides widthSizeClass) {
                Box {
                    NavHost(
                        navController = navController,
                        startDestination = Route.Splash,
                        modifier = Modifier,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(350)
                            ) + fadeIn(tween(350))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(350)
                            ) + fadeOut(tween(350))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(350)
                            ) + fadeIn(tween(350))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(350)
                            ) + fadeOut(tween(350))
                        }
                    ) {
                        composable<Route.Splash>(
                            exitTransition = { fadeOut(tween(500)) },
                            popExitTransition = { fadeOut(tween(500)) }
                        ) {
                            SplashPage(
                                onNavigateToLogin = {
                                    navController.navigate(Route.Login) {
                                        popUpTo<Route.Splash> { inclusive = true }
                                    }
                                },
                                onNavigateToMain = {
                                    navController.navigate(Route.Main) {
                                        popUpTo<Route.Splash> { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<Route.Login>(
                            enterTransition = { fadeIn(tween(500)) }
                        ) {
                            val hasPrevious = navController.previousBackStackEntry != null
                            LoginPage(
                                onNavigateToMain = {
                                    navController.navigate(Route.Main) {
                                        popUpTo<Route.Main> { inclusive = true }
                                    }
                                },
                                onBack = if (hasPrevious) {
                                    { navController.popBackStack() }
                                } else null,
                                onNavigateToSettings = {
                                    navController.navigate(Route.Settings)
                                }
                            )
                        }

                        composable<Route.Main> {
                            MainPage(
                                onNavigateToLogin = {
                                    navController.navigate(Route.Login) {
                                        popUpTo<Route.Main> { inclusive = true }
                                    }
                                },
                                onNavigateToAddAccount = {
                                    navController.navigate(Route.Login)
                                },
                                onNavigateToSubPage = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }

                        composable<Route.IncomeDetail> {
                            IncomeDetailPage(
                                isLastMonth = it.toRoute<Route.IncomeDetail>().isLastMonth,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<Route.Income> {
                            IncomePage(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<Route.Mailbox> {
                            MailboxPage(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<Route.Settings> {
                            SettingsContent(
                                showAccountManagement = false,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    // Compact/Medium 布局下 MainPage 有底部 NavigationBar (80dp)，
                    // Snackbar 需要额外偏移以避免遮挡
                    val navBarInset =
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    val bottomOffset = when (widthSizeClass) {
                        WindowWidthSizeClass.Compact, WindowWidthSizeClass.Medium -> navBarInset + 80.dp
                        else -> navBarInset + 8.dp
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .padding(bottom = bottomOffset)
                    ) { data ->
                        Snackbar(
                            snackbarData = data,
                            shape = RoundedCornerShape(8.dp),
                            containerColor = colors.surface,
                            contentColor = colors.textColor
                        )
                    }
                }
            }
        }
    }
}

