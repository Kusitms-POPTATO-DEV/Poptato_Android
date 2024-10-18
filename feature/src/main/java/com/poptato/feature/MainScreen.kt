package com.poptato.feature

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetLayout
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.poptato.core.enums.BottomNavType
import com.poptato.design_system.Day
import com.poptato.design_system.FINISH_APP_GUIDE
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray80
import com.poptato.design_system.Month
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.design_system.Year
import com.poptato.domain.model.enums.BottomSheetType
import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import com.poptato.domain.model.request.todo.TodoContentModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.navigation.NavRoutes
import com.poptato.navigation.historyNavGraph
import com.poptato.navigation.loginNavGraph
import com.poptato.navigation.myPageNavGraph
import com.poptato.navigation.backlogNavGraph
import com.poptato.navigation.splashNavGraph
import com.poptato.navigation.todayNavGraph
import com.poptato.navigation.yesterdayListNavGraph
import com.poptato.ui.common.TodoBottomSheetContent
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
    val sheetState = androidx.compose.material.rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val animationDuration = 300
    val showBottomSheet: (TodoItemModel) -> Unit = { item: TodoItemModel ->
        viewModel.onSelectedTodoItem(item)
        scope.launch { sheetState.show() }
    }
    val todoBottomSheetClosedFlow = MutableSharedFlow<Unit>()
    val updateDeadlineFlow = MutableSharedFlow<String>()
    val deleteTodoFlow = MutableSharedFlow<Long>()
    val activateItemFlow = MutableSharedFlow<Long>()
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
                    scope.launch {
                        todoBottomSheetClosedFlow.emit(Unit)
                    }
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
                                }
                            )
                        }
                        BottomSheetType.FullDate -> {
                            DatePickerBottomSheet(
                                onDismissRequest = { bottomSheetType = BottomSheetType.Main },
                                bottomSheetType = BottomSheetType.FullDate,
                                onFullDateSelected = { date ->
                                    viewModel.onUpdatedDeadline(date)
                                    scope.launch { updateDeadlineFlow.emit(date) }
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
                }
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
                        loginNavGraph(navController = navController)
                        yesterdayListNavGraph(navController = navController)
                        myPageNavGraph(navController = navController)
                        backlogNavGraph(
                            navController = navController,
                            showBottomSheet = showBottomSheet,
                            todoBottomSheetClosedFlow = todoBottomSheetClosedFlow,
                            updateDeadlineFlow = updateDeadlineFlow,
                            deleteTodoFlow = deleteTodoFlow,
                            activateItemFlow = activateItemFlow
                        )
                        todayNavGraph(navController = navController)
                        historyNavGraph(navController = navController)
                    }
                }
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
            iconId = if(type == BottomNavType.TODAY) R.drawable.ic_today_selected else R.drawable.ic_today_unselected,
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
                when (type) {
                    BottomNavType.TODAY -> {
                        onClick(NavRoutes.TodayScreen.route)
                    }

                    BottomNavType.BACK_LOG -> {
                        onClick(NavRoutes.BacklogScreen.route)
                    }

                    BottomNavType.HISTORY -> {
                        onClick(NavRoutes.HistoryScreen.route)
                    }

                    BottomNavType.SETTINGS -> {
                        onClick(NavRoutes.MyPageScreen.route)
                    }

                    BottomNavType.DEFAULT -> {}
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
