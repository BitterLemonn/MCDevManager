package com.lemon.mcdevmanagermp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lemon.mcdevmanagermp.ui.pages.income.IncomePage
import com.lemon.mcdevmanagermp.ui.pages.incomeDetail.IncomeDetailPage
import com.lemon.mcdevmanagermp.ui.pages.login.LoginPage
import com.lemon.mcdevmanagermp.ui.pages.main.MainPage
import com.lemon.mcdevmanagermp.ui.pages.settings.SettingsContent
import com.lemon.mcdevmanagermp.ui.pages.splash.SplashPage

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        modifier = modifier,
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
                onBack = if (hasPrevious) {{ navController.popBackStack() }} else null,
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

        composable<Route.Settings> {
            SettingsContent(
                showAccountManagement = false,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
