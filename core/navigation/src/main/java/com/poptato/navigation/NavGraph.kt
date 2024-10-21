package com.poptato.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.poptato.backlog.BacklogScreen
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.login.KaKaoLoginScreen
import com.poptato.mypage.MyPageScreen
import com.poptato.mypage.viewer.NoticeViewerScreen
import com.poptato.mypage.viewer.FAQViewerScreen
import com.poptato.mypage.viewer.PolicyViewerScreen
import com.poptato.setting.servicedelete.ServiceDeleteScreen
import com.poptato.setting.userdata.UserDataScreen
import com.poptato.splash.SplashScreen
import com.poptato.today.TodayScreen
import com.poptato.yesterdaylist.YesterdayListScreen
import com.poptato.yesterdaylist.allcheck.AllCheckScreen
import com.potato.history.HistoryScreen
import kotlinx.coroutines.flow.SharedFlow

fun NavGraphBuilder.splashNavGraph(navController: NavHostController) {
    navigation(
        startDestination = NavRoutes.SplashScreen.route,
        route = NavRoutes.SplashGraph.route
    ) {
        composable(NavRoutes.SplashScreen.route) {
            SplashScreen(
                goToKaKaoLogin = {
                    navController.navigate(NavRoutes.KaKaoLoginGraph.route) {
                        popUpTo(NavRoutes.SplashScreen.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.loginNavGraph(
    navController: NavHostController,
    showSnackBar: (String) -> Unit
) {
    navigation(
        startDestination = NavRoutes.KaKaoLoginScreen.route,
        route = NavRoutes.KaKaoLoginGraph.route
    ) {
        composable(NavRoutes.KaKaoLoginScreen.route) {
            KaKaoLoginScreen(
                goToBacklog = {
                    navController.navigate(NavRoutes.BacklogScreen.route) {
                        popUpTo(NavRoutes.KaKaoLoginScreen.route) {
                            inclusive = true
                        }
                    }
                },
                showSnackBar = showSnackBar
            )
        }
    }
}

fun NavGraphBuilder.backlogNavGraph(
    navController: NavHostController,
    showBottomSheet: (TodoItemModel) -> Unit,
    updateDeadlineFlow: SharedFlow<String?>,
    deleteTodoFlow: SharedFlow<Long>,
    activateItemFlow: SharedFlow<Long>,
    updateBookmarkFlow: SharedFlow<Long>,
    showSnackBar: (String) -> Unit
) {
    navigation(
        startDestination = NavRoutes.BacklogScreen.route,
        route = NavRoutes.BacklogGraph.route
    ) {
        composable(NavRoutes.BacklogScreen.route) {
            BacklogScreen(
                goToYesterdayList = { navController.navigate(NavRoutes.YesterdayListScreen.route) },
                showBottomSheet = showBottomSheet,
                updateDeadlineFlow = updateDeadlineFlow,
                deleteTodoFlow = deleteTodoFlow,
                activateItemFlow = activateItemFlow,
                updateBookmarkFlow = updateBookmarkFlow,
                showSnackBar = showSnackBar
            )
        }
    }
}

fun NavGraphBuilder.yesterdayListNavGraph(navController: NavHostController) {
    navigation(
        startDestination = NavRoutes.YesterdayListScreen.route,
        route = NavRoutes.YesterdayListGraph.route
    ) {
        composable(NavRoutes.YesterdayListScreen.route) {
            YesterdayListScreen(
                goBackToBacklog = { navController.popBackStack() },
                showAllCheckPage = { navController.navigate(NavRoutes.YesterdayAllCheckScreen.route) }
            )
        }

        composable(NavRoutes.YesterdayAllCheckScreen.route) {
            AllCheckScreen(
                goBackToBacklog = {
                    navController.navigate(NavRoutes.BacklogScreen.route) {
                        popUpTo(NavRoutes.BacklogScreen.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.myPageNavGraph(navController: NavHostController) {
    navigation(
        startDestination = NavRoutes.MyPageScreen.route,
        route = NavRoutes.MyPageGraph.route
    ) {
        composable(NavRoutes.MyPageScreen.route) {
            MyPageScreen(
                goToUserDataPage = { navController.navigate(NavRoutes.UserDataScreen.route) },
                goToNoticeViewerPage = { navController.navigate(NavRoutes.NoticeViewScreen.route) },
                goToFAQViewerPage = { navController.navigate(NavRoutes.FAQViewScreen.route) },
                goToPolicyViewerPage = { navController.navigate(NavRoutes.PolicyViewScreen.route) }
            )
        }

        composable(NavRoutes.NoticeViewScreen.route) {
            NoticeViewerScreen(
                goBackToMyPage = { navController.navigate(NavRoutes.MyPageScreen.route) {
                    popUpTo(NavRoutes.MyPageScreen.route) { inclusive = true }
                } }
            )
        }

        composable(NavRoutes.FAQViewScreen.route) {
            FAQViewerScreen(
                goBackToMyPage = { navController.navigate(NavRoutes.MyPageScreen.route) {
                    popUpTo(NavRoutes.MyPageScreen.route) { inclusive = true }
                } }
            )
        }

        composable(NavRoutes.PolicyViewScreen.route) {
            PolicyViewerScreen(
                goBackToMyPage = { navController.navigate(NavRoutes.MyPageScreen.route) {
                    popUpTo(NavRoutes.MyPageScreen.route) { inclusive = true }
                } }
            )
        }

        composable(NavRoutes.ServiceDeleteScreen.route) {
            ServiceDeleteScreen(
                goBackToSetting = { navController.popBackStack() },
                goBackToLogIn = { navController.navigate(NavRoutes.KaKaoLoginScreen.route) }
            )
        }

        composable(NavRoutes.UserDataScreen.route) {
            UserDataScreen(
                goBackToMyPage = { navController.popBackStack() },
                goBackToLogIn = { navController.navigate(NavRoutes.KaKaoLoginScreen.route) },
                goToServiceDelete = { navController.navigate(NavRoutes.ServiceDeleteScreen.route) },
            )
        }
    }
}

fun NavGraphBuilder.todayNavGraph(
    navController: NavHostController,
    showSnackBar: (String) -> Unit
) {
    navigation(startDestination = NavRoutes.TodayScreen.route, route = NavRoutes.TodayGraph.route) {
        composable(NavRoutes.TodayScreen.route) {
            TodayScreen(
                goToBacklog = { navController.navigate(NavRoutes.BacklogScreen.route) },
                showSnackBar = showSnackBar
            )
        }
    }
}

fun NavGraphBuilder.historyNavGraph(navController: NavHostController) {
    navigation(
        startDestination = NavRoutes.HistoryScreen.route,
        route = NavRoutes.HistoryGraph.route
    ) {
        composable(NavRoutes.HistoryScreen.route) {
            HistoryScreen()
        }
    }
}