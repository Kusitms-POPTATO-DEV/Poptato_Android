package com.poptato.feature

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Snackbar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.poptato.core.enums.BottomNavType
import com.poptato.design_system.BgSnackBar
import com.poptato.design_system.FINISH_APP_GUIDE
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.PoptatoTypo
import com.poptato.domain.model.enums.BottomSheetType
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.feature.component.BottomNavBar
import com.poptato.navigation.NavRoutes
import com.poptato.navigation.historyNavGraph
import com.poptato.navigation.loginNavGraph
import com.poptato.navigation.myPageNavGraph
import com.poptato.navigation.backlogNavGraph
import com.poptato.navigation.splashNavGraph
import com.poptato.navigation.todayNavGraph
import com.poptato.navigation.yesterdayListNavGraph
import com.poptato.ui.common.CommonSnackBar
import com.poptato.ui.common.DatePickerBottomSheet
import com.poptato.ui.common.TodoBottomSheet
import com.poptato.ui.util.DismissKeyboardOnClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MainScreen() {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHost = remember { SnackbarHostState() }
    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val animationDuration = 300
    val showBottomSheet: (TodoItemModel) -> Unit = { item: TodoItemModel ->
        viewModel.onSelectedTodoItem(item)
        scope.launch { sheetState.show() }
    }
    val updateDeadlineFlow = MutableSharedFlow<String?>()
    val deleteTodoFlow = MutableSharedFlow<Long>()
    val activateItemFlow = MutableSharedFlow<Long>()
    val updateBookmarkFlow = MutableSharedFlow<Long>()
    var bottomSheetType by remember { mutableStateOf(BottomSheetType.Main) }
    var backPressedOnce by remember { mutableStateOf(false) }
    val backPressHandler: () -> Unit = {
        if (backPressedOnce) {
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, FINISH_APP_GUIDE, Toast.LENGTH_SHORT).show()
            scope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }
    val showSnackBar: (String) -> Unit = { message ->
        scope.launch {
            snackBarHost.showSnackbar(message = message)
        }
    }

    if (uiState.bottomNavType != BottomNavType.DEFAULT) {
        BackHandler(onBack = backPressHandler)
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .collect { backStackEntry ->
                viewModel.setBottomNavType(backStackEntry.destination.route)
            }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.isVisible }
            .distinctUntilChanged()
            .collect { isVisible ->
                if (!isVisible) {
                    bottomSheetType = BottomSheetType.Main
                }
            }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is MainEvent.ShowTodoBottomSheet -> {
                    sheetState.show()
                }
            }
        }
    }

    DismissKeyboardOnClick {
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetGesturesEnabled = bottomSheetType == BottomSheetType.Main,
            sheetContent = {
                AnimatedContent(
                    targetState = bottomSheetType,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(324.dp)
                        .background(Gray100),
                    label = ""
                ) { currentSheet ->
                    when (currentSheet) {
                        BottomSheetType.Main -> {
                            TodoBottomSheet(
                                item = uiState.selectedTodoItem,
                                setDeadline = {
                                    scope.launch { updateDeadlineFlow.emit(it) }
                                },
                                onClickShowDatePicker = { bottomSheetType = BottomSheetType.FullDate },
                                onClickBtnDelete = {
                                    scope.launch {
                                        deleteTodoFlow.emit(it)
                                        sheetState.hide()
                                    }
                                },
                                onClickBtnModify = {
                                    scope.launch {
                                        activateItemFlow.emit(it)
                                        sheetState.hide()
                                    }
                                },
                                onClickBtnBookmark = {
                                    scope.launch {
                                        updateBookmarkFlow.emit(it)
                                    }
                                }
                            )
                        }
                        BottomSheetType.FullDate -> {
                            DatePickerBottomSheet(
                                onDismissRequest = { bottomSheetType = BottomSheetType.Main },
                                bottomSheetType = BottomSheetType.FullDate,
                                onFullDateSelected = { date ->
                                    viewModel.onUpdatedDeadline(date)
                                    scope.launch {
                                        updateDeadlineFlow.emit(date)
                                        sheetState.hide()
                                    }
                                }
                            )
                        }
                        BottomSheetType.Calendar -> TODO("캘린더 바텀시트 컴포저블을 여기에 추가")
                        BottomSheetType.SubDate -> {
                            DatePickerBottomSheet(
                                onDismissRequest = { bottomSheetType = BottomSheetType.Calendar },
                                bottomSheetType = BottomSheetType.SubDate
                            )
                        }
                    }
                }
            },
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scrimColor = Color(0, 0, 0, 128)
        ) {
            Scaffold(
                bottomBar = {
                    AnimatedVisibility(
                        visible = uiState.bottomNavType != BottomNavType.DEFAULT,
                        enter = slideInHorizontally(animationSpec = tween(durationMillis = animationDuration)),
                        exit = slideOutHorizontally(animationSpec = tween(durationMillis = animationDuration)),
                        modifier = Modifier.background(Gray100)
                    ) {
                        BottomNavBar(
                            type = uiState.bottomNavType,
                            onClick = { route: String ->
                                if (navController.currentDestination?.route != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.currentDestination?.route!!) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            modifier = Modifier.navigationBarsPadding()
                        )
                    }
                },
                snackbarHost = { CommonSnackBar(hostState = snackBarHost) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.SplashGraph.route,
                        exitTransition = { ExitTransition.None },
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                tween(animationDuration)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(animationDuration)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(animationDuration)
                            )
                        }
                    ) {
                        splashNavGraph(navController = navController)
                        loginNavGraph(navController = navController, showSnackBar = showSnackBar)
                        yesterdayListNavGraph(navController = navController)
                        myPageNavGraph(navController = navController)
                        backlogNavGraph(
                            navController = navController,
                            showBottomSheet = showBottomSheet,
                            updateDeadlineFlow = updateDeadlineFlow,
                            deleteTodoFlow = deleteTodoFlow,
                            activateItemFlow = activateItemFlow,
                            updateBookmarkFlow = updateBookmarkFlow,
                            showSnackBar = showSnackBar
                        )
                        todayNavGraph(navController = navController, showSnackBar = showSnackBar)
                        historyNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}