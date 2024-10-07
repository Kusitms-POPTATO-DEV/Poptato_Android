package com.poptato.feature

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.poptato.core.enums.BottomNavType
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray80
import com.poptato.design_system.History
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.design_system.Settings
import com.poptato.design_system.Today
import com.poptato.navigation.NavRoutes
import com.poptato.navigation.loginNavGraph
import com.poptato.navigation.mainNavGraph
import com.poptato.navigation.splashNavGraph
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MainScreen() {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val slideDuration = 300

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .collect { backStackEntry ->
                viewModel.setBottomNavType(backStackEntry.destination.route)
            }
    }

    Scaffold(
        bottomBar = {
            if (uiState.bottomNavType != BottomNavType.DEFAULT) {
                BottomNavBar(
                    type = uiState.bottomNavType,
                    onClick = { route: String ->
                        navController.navigate(route)
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .systemBarsPadding()
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SplashGraph.route,
                exitTransition = { ExitTransition.None },
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(slideDuration)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(slideDuration)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(slideDuration)
                    )
                }
            ) {
                splashNavGraph(navController = navController)
                loginNavGraph(navController = navController)
                mainNavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun BottomNavBar(
    type: BottomNavType = BottomNavType.TODAY,
    onClick: (String) -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Gray100),
        horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            iconId = if(type == BottomNavType.TODAY) R.drawable.ic_calendar_selected else R.drawable.ic_calendar_unselected,
            isSelected = type == BottomNavType.TODAY,
            type = BottomNavType.TODAY,
            onClick = onClick
        )
        BottomNavItem(
            iconId = if(type == BottomNavType.BACK_LOG) R.drawable.ic_list_selected else R.drawable.ic_list_unselected,
            isSelected = type == BottomNavType.BACK_LOG,
            type = BottomNavType.BACK_LOG,
            onClick = onClick
        )
        BottomNavItem(
            iconId = if(type == BottomNavType.HISTORY) R.drawable.ic_clock_selected else R.drawable.ic_clock_unselected,
            isSelected = type == BottomNavType.HISTORY,
            type = BottomNavType.HISTORY,
            onClick = onClick
        )
        BottomNavItem(
            iconId = if(type == BottomNavType.SETTINGS) R.drawable.ic_settings_selected else R.drawable.ic_settings_unselected,
            isSelected = type == BottomNavType.SETTINGS,
            type = BottomNavType.SETTINGS,
            onClick = onClick
        )
    }
}

@Composable
fun BottomNavItem(
    iconId: Int = -1,
    isSelected: Boolean = false,
    type: BottomNavType = BottomNavType.DEFAULT,
    onClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .size(width = 42.dp, height = 46.dp)
            .clickable {
                when(type) {
                    BottomNavType.TODAY -> TODO()
                    BottomNavType.BACK_LOG -> { onClick(NavRoutes.BacklogScreen.route) }
                    BottomNavType.HISTORY -> TODO()
                    BottomNavType.SETTINGS -> TODO()
                    BottomNavType.DEFAULT -> TODO()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "",
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = type.navName,
            style = PoptatoTypo.xsMedium,
            color = if (isSelected) Primary60 else Gray80
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewBottomNav() {
    BottomNavBar()
}