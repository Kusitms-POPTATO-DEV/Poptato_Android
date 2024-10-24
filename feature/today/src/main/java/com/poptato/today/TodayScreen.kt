package com.poptato.today

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.poptato.core.util.TimeFormatter
import com.poptato.core.util.move
import com.poptato.design_system.BOOKMARK
import com.poptato.design_system.BtnGetTodoText
import com.poptato.design_system.DEADLINE
import com.poptato.design_system.DEADLINE_DDAY
import com.poptato.design_system.ERROR_GENERIC_MESSAGE
import com.poptato.design_system.EmptyTodoTitle
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray40
import com.poptato.design_system.Gray70
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary100
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.design_system.TodayTopBarSub
import com.poptato.domain.model.enums.TodoStatus
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.ui.common.BookmarkItem
import com.poptato.ui.common.PoptatoCheckBox
import com.poptato.ui.common.TopBar
import com.poptato.ui.util.DragDropListState
import com.poptato.ui.util.rememberDragDropListState
import com.poptato.ui.util.toPx
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    goToBacklog: () -> Unit = {},
    showSnackBar: (String) -> Unit
) {
    val viewModel: TodayViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val date = TimeFormatter.getTodayMonthDay()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when(event) {
                is TodayEvent.OnFailedUpdateTodayList -> {
                    showSnackBar(ERROR_GENERIC_MESSAGE)
                }
            }
        }
    }

    if (uiState.isFinishedInitialization) {
        TodayContent(
            date = date,
            uiState = uiState,
            onCheckedChange = { status, id ->
                viewModel.onCheckedTodo(status = status, id = id)
            },
            onClickBtnGetTodo = { goToBacklog() },
            onItemSwiped = { itemToRemove -> viewModel.swipeTodayItem(itemToRemove) },
            onDragEnd = { from, to -> viewModel.moveItem(from, to) }
        )
    }
}

@Composable
fun TodayContent(
    date: String = "",
    uiState: TodayPageState = TodayPageState(),
    onCheckedChange: (TodoStatus, Long) -> Unit = {_, _ ->},
    onClickBtnGetTodo: () -> Unit = {},
    onItemSwiped: (TodoItemModel) -> Unit = {},
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        TopBar(
            titleText = date,
            titleTextStyle = PoptatoTypo.xxxLSemiBold,
            titleTextColor = Gray00,
            subText = TodayTopBarSub,
            subTextStyle = PoptatoTypo.mdMedium,
            subTextColor = Gray40,
            isTodayTopBar = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.todayList.isEmpty()) EmptyTodoView(
                onClickBtnGetTodo = onClickBtnGetTodo
            )
            else TodayTodoList(
                list = uiState.todayList,
                onCheckedChange = onCheckedChange,
                onItemSwiped = onItemSwiped,
                onDragEnd = onDragEnd
            )
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun TodayTodoList(
    list: List<TodoItemModel> = emptyList(),
    onCheckedChange: (TodoStatus, Long) -> Unit = { _, _ -> },
    onItemSwiped: (TodoItemModel) -> Unit = {},
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    var uiList by remember { mutableStateOf(list.toMutableList()) }
    var draggedItem by remember { mutableStateOf<TodoItemModel?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(list) {
        uiList = list.toMutableList()
    }

    fun moveItemInUI(fromIndex: Int, toIndex: Int) {
        val lastIncompleteIndex = uiList.indexOfLast { it.todoStatus == TodoStatus.INCOMPLETE }
        var safeToIndex = toIndex

        if (lastIncompleteIndex in fromIndex..<toIndex) {
            safeToIndex = lastIncompleteIndex
        } else if (lastIncompleteIndex in toIndex..<fromIndex) {
            safeToIndex = lastIncompleteIndex + 1
        }

        if (fromIndex != safeToIndex) {
            uiList.move(fromIndex, safeToIndex)
            onDragEnd(fromIndex, safeToIndex)
        }
    }
    val dragDropState = rememberDragDropListState(
        lazyListState = rememberLazyListState(),
        onMove = { from, to ->
            if (from != to) {
                moveItemInUI(from, to)
            }
        }
    )

    LazyColumn(
        state = dragDropState.lazyListState,
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragDropState.onDragStart(offset)
                        draggedItem = list[dragDropState.currentIndexOfDraggedItem
                            ?: return@detectDragGesturesAfterLongPress]
                        isDragging = true
                    },
                    onDragEnd = {
                        dragDropState.onDragInterrupted()
                        draggedItem = null
                        isDragging = false
                    },
                    onDragCancel = {
                        dragDropState.onDragInterrupted()
                        draggedItem = null
                        isDragging = false
                    },
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropState.onDrag(offset)
                        if (dragDropState.overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress
                        dragDropState
                            .checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                dragDropState.overscrollJob = scope.launch {
                                    val adjustedScroll = it * 0.5f
                                    dragDropState.lazyListState.scrollBy(adjustedScroll)
                                }
                            } ?: run { dragDropState.overscrollJob?.cancel() }
                    }
                )
            }
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        itemsIndexed(items = uiList, key = { index, item -> item.todoId }) { index, item ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val isDragged = index == dragDropState.currentIndexOfDraggedItem

            TodayTodoItem(
                item = item,
                onCheckedChange = onCheckedChange,
                modifier = Modifier
                    .zIndex(if (index == dragDropState.currentIndexOfDraggedItem) 1f else 0f)
                    .graphicsLayer {
                        translationY =
                            dragDropState.elementDisplacement.takeIf { index == dragDropState.currentIndexOfDraggedItem }
                                ?: 0f
                        scaleX = if (isDragged) 1.05f else 1f
                        scaleY = if (isDragged) 1.05f else 1f
                    }
                    .offset { IntOffset(offsetX.toInt(), 0) }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 200f) {
                                    onItemSwiped(item)
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                if (offsetX + dragAmount >= 0f) {
                                    offsetX += dragAmount
                                }
                            }
                        )
                    }
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null
                    )
                    .border(
                        if (isDragged) BorderStroke(1.dp, Color.White) else BorderStroke(
                            0.dp,
                            Color.Transparent
                        ),
                        RoundedCornerShape(8.dp)
                    )

            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun TodayTodoItem(
    item: TodoItemModel = TodoItemModel(),
    onCheckedChange: (TodoStatus, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    var showAnimation by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = showAnimation, label = "")

    LaunchedEffect(transition) {
        snapshotFlow { transition.currentState }
            .filter { it }
            .collect {
                showAnimation = false
            }
    }

    val elevation by transition.animateDp(
        transitionSpec = { spring() },
        label = ""
    ) { isAnimating ->
        if (isAnimating) 8.dp else 0.dp
    }

    val scale by transition.animateFloat(
        transitionSpec = { spring() },
        label = ""
    ) { isAnimating ->
        if (isAnimating) 1.1f else 1f
    }

    val yOffset by transition.animateFloat(
        transitionSpec = { spring() },
        label = ""
    ) { isAnimating ->
        if (isAnimating) -50f else 0f
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, yOffset.toInt()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shadowElevation = elevation.toPx()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Gray95)
        ) {

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = if (item.isBookmark || item.dDay != null) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                if (item.isBookmark) {
                    BookmarkItem()
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (item.dDay != null) Text(
                    text = if(item.dDay != 0) String.format(DEADLINE, item.dDay) else DEADLINE_DDAY,
                    style = PoptatoTypo.xsSemiBold,
                    color = Gray70
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 16.dp,
                        top = if (item.isBookmark || item.dDay != null) 8.dp else 16.dp
                    )
                    .padding(start = 16.dp, end = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PoptatoCheckBox(
                    isChecked = item.todoStatus == TodoStatus.COMPLETED,
                    onCheckedChange = {
                        onCheckedChange(item.todoStatus, item.todoId)
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.content,
                    color = Gray00,
                    style = PoptatoTypo.mdRegular,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyTodoView(
    onClickBtnGetTodo: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = EmptyTodoTitle,
            color = Gray40,
            style = PoptatoTypo.lgMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .size(width = 132.dp, height = 37.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Primary60)
                .clickable { onClickBtnGetTodo() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_list_selected),
                contentDescription = "",
                tint = Primary100,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = BtnGetTodoText,
                style = PoptatoTypo.smSemiBold,
                color = Primary100
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewToday() {
//    TodayContent()
}