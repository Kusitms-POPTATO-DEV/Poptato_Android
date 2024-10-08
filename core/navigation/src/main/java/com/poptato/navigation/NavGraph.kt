package com.poptato.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.poptato.backlog.BacklogScreen
import com.poptato.login.KaKaoLoginScreen
import com.poptato.splash.SplashScreen

fun NavGraphBuilder.splashNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.SplashScreen.route, route = NavRoutes.SplashGraph.route) {
        composable(NavRoutes.SplashScreen.route) {
            SplashScreen(
                goToKaKaoLogin = { navController.navigate(NavRoutes.KaKaoLoginGraph.route) }
            )
        }
    }
}

fun NavGraphBuilder.loginNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.KaKaoLoginScreen.route, route = NavRoutes.KaKaoLoginGraph.route) {
        composable(NavRoutes.KaKaoLoginScreen.route) {
            KaKaoLoginScreen(
                goToBacklog = { navController.navigate(NavRoutes.BacklogScreen.route) }
            )
        }
    }
}

fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.BacklogScreen.route, route = NavRoutes.MainGraph.route) {
        composable(NavRoutes.BacklogScreen.route) {
            BacklogScreen()
        }
    }
}

fun NavGraphBuilder.historyNavGraph(navController: NavHostController) {
    navigation(startDestination = NavRoutes.HistoryScreen.route, route = NavRoutes.HistoryGraph.route) {
        composable(NavRoutes.HistoryScreen.route) {
        //    HistoryScreen()
        }
    }
}