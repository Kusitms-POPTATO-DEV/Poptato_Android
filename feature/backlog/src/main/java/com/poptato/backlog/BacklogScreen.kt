package com.poptato.backlog

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.poptato.core.util.move
import com.poptato.design_system.BACKLOG_YESTERDAY_TASK_GUIDE
import com.poptato.design_system.BacklogHint
import com.poptato.design_system.COMPLETE_DELETE_TODO
import com.poptato.design_system.CONFIRM_ACTION
import com.poptato.design_system.DEADLINE
import com.poptato.design_system.DEADLINE_DDAY
import com.poptato.design_system.ERROR_GENERIC_MESSAGE
import com.poptato.design_system.EmptyBacklogTitle
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray70
import com.poptato.design_system.Gray80
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.domain.model.enums.TodoStatus
import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import com.poptato.domain.model.request.todo.TodoContentModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.ui.common.BookmarkItem
import com.poptato.ui.common.TopBar
import com.poptato.ui.util.DragDropListState
import com.poptato.ui.util.rememberDragDropListState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Composable
fun BacklogScreen(
    goToYesterdayList: () -> Unit = {},
    showBottomSheet: (TodoItemModel) -> Unit = {},
    updateDeadlineFlow: SharedFlow<String?>,
    deleteTodoFlow: SharedFlow<Long>,
    activateItemFlow: SharedFlow<Long>,
    updateBookmarkFlow: SharedFlow<Long>,
    showSnackBar: (String) -> Unit
) {
    val viewModel: BacklogViewModel = hiltViewModel()
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val uiState: BacklogPageState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeItemId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(activateItemFlow) {
        activateItemFlow.collect { id ->
            activeItemId = id
        }
    }

    LaunchedEffect(deleteTodoFlow) {
        deleteTodoFlow.collect {
            viewModel.deleteBacklog(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is BacklogEvent.OnFailedUpdateBacklogList -> {
                    showSnackBar(ERROR_GENERIC_MESSAGE)
                }
                is BacklogEvent.OnSuccessDeleteBacklog -> {
                    showSnackBar(COMPLETE_DELETE_TODO)
                }
            }
        }
    }

    LaunchedEffect(updateDeadlineFlow) {
        updateDeadlineFlow.collect {
            viewModel.setDeadline(it, uiState.selectedItem.todoId)
        }
    }

    LaunchedEffect(updateBookmarkFlow) {
        updateBookmarkFlow.collect {
            viewModel.updateBookmark(it)
        }
    }

    if (uiState.isFinishedInitialization) {
        BacklogContent(
            uiState = uiState,
            onValueChange = { newValue -> viewModel.onValueChange(newValue) },
            createBacklog = { newItem -> viewModel.createBacklog(newItem) },
            onItemSwiped = { itemToRemove -> viewModel.swipeBacklogItem(itemToRemove) },
            onClickYesterdayList = { goToYesterdayList() },      // TODO 테스트용: "어제 리스트 체크하기" 스낵바 생성 후 변경 예정
            onClickBtnTodoSettings = {
                showBottomSheet(uiState.backlogList[it])
                viewModel.onSelectedItem(uiState.backlogList[it])
            },
            interactionSource = interactionSource,
            activeItemId = activeItemId,
            onClearActiveItem = { activeItemId = null },
            onTodoItemModified = { id: Long, content: String ->
                viewModel.modifyTodo(
                    item = ModifyTodoRequestModel(
                        todoId = id,
                        content = TodoContentModel(
                            content = content
                        )
                    )
                )
            },
            resetNewItemFlag = { viewModel.updateNewItemFlag(false) },
            onDragEnd = { from, to -> viewModel.moveItem(from, to) }
        )
    }
}

@Composable
fun BacklogContent(
    uiState: BacklogPageState = BacklogPageState(),
    onValueChange: (String) -> Unit = {},
    createBacklog: (String) -> Unit = {},
    onClickYesterdayList: () -> Unit = {},
    onItemSwiped: (TodoItemModel) -> Unit = {},
    onClickBtnTodoSettings: (Int) -> Unit = {},
    interactionSource: MutableInteractionSource,
    activeItemId: Long?,
    onClearActiveItem: () -> Unit = {},
    onTodoItemModified: (Long, String) -> Unit = {_,_ ->},
    resetNewItemFlag: () -> Unit = {},
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        TopBar(
            titleText = com.poptato.design_system.TODO,
            subText = uiState.backlogList.size.toString(),
            subTextStyle = PoptatoTypo.xLSemiBold,
            subTextColor = Primary60
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                CreateBacklogTextFiled(
                    onValueChange = onValueChange,
                    taskInput = uiState.taskInput,
                    createBacklog = createBacklog
                )

                if (uiState.backlogList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = EmptyBacklogTitle,
                            style = PoptatoTypo.lgMedium,
                            textAlign = TextAlign.Center,
                            color = Gray80
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))

                    BacklogTaskList(
                        taskList = uiState.backlogList,
                        onItemSwiped = onItemSwiped,
                        onClickBtnTodoSettings = onClickBtnTodoSettings,
                        activeItemId = activeItemId,
                        onClearActiveItem = onClearActiveItem,
                        onTodoItemModified = onTodoItemModified,
                        isNewItemCreated = uiState.isNewItemCreated,
                        resetNewItemFlag = resetNewItemFlag,
                        onDragEnd = onDragEnd
                    )
                }
            }

            if (!uiState.isYesterdayListEmpty) {
                BacklogGuideItem(
                    onClickYesterdayList = onClickYesterdayList,
                    modifier = Modifier
                        .align(Alignment.BottomCenter),
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateBacklogTextFiled(
    taskInput: String = "",
    onValueChange: (String) -> Unit = {},
    createBacklog: (String) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val imeVisible = WindowInsets.isImeVisible

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            focusManager.clearFocus()
        }
    }


    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isFocused) Gray00 else Gray70,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        BasicTextField(
            value = taskInput,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(start = 16.dp, end = 28.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            textStyle = PoptatoTypo.mdRegular,
            cursorBrush = SolidColor(Gray00),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if(taskInput.isNotEmpty()) createBacklog(taskInput)
                    onValueChange("")
                }
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (taskInput.isEmpty() && !isFocused) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "",
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = BacklogHint,
                            style = PoptatoTypo.mdMedium,
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (taskInput.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "",
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun BacklogTaskList(
    taskList: List<TodoItemModel> = emptyList(),
    onItemSwiped: (TodoItemModel) -> Unit = {},
    onClickBtnTodoSettings: (Int) -> Unit = {},
    activeItemId: Long?,
    onClearActiveItem: () -> Unit = {},
    onTodoItemModified: (Long, String) -> Unit = {_,_ ->},
    isNewItemCreated: Boolean = false,
    resetNewItemFlag: () -> Unit = {},
    onDragEnd: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    var uiList by remember { mutableStateOf(taskList.toMutableList()) }
    var draggedItem by remember { mutableStateOf<TodoItemModel?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(taskList) {
        uiList = taskList.toMutableList()
    }

    fun moveItemInUI(fromIndex: Int, toIndex: Int) {
        if (fromIndex != toIndex) {
            uiList.move(fromIndex, toIndex)
        }

        onDragEnd(fromIndex, toIndex)
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
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragDropState.onDragStart(offset)
                        draggedItem = taskList[dragDropState.currentIndexOfDraggedItem
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
    ) {
        itemsIndexed(uiList, key = { _, item -> item.todoId }) { index, item ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val isDragged = index == dragDropState.currentIndexOfDraggedItem
            val isActive = activeItemId == item.todoId

            Box(
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
                                if (offsetX < -200f) {
                                    onItemSwiped(item)
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                if (offsetX + dragAmount <= 0f) {
                                    offsetX += dragAmount
                                }
                            }
                        )
                    }
                    .then(
                        if (!isDragging) {
                            Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null
                            )
                        } else {
                            Modifier
                        }
                    )
                    .border(
                        if (isDragged) BorderStroke(1.dp, Color.White) else BorderStroke(
                            0.dp,
                            Color.Transparent
                        ),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                BacklogItem(
                    item = item,
                    index = index,
                    isActive = isActive,
                    onClickBtnTodoSettings = onClickBtnTodoSettings,
                    onClearActiveItem = onClearActiveItem,
                    onTodoItemModified = onTodoItemModified
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item { Spacer(modifier = Modifier.height(45.dp)) }
    }

    LaunchedEffect(isNewItemCreated) {
        if (isNewItemCreated) {
            dragDropState.lazyListState.scrollToItem(0)
            resetNewItemFlag()
        }
    }
}

@Composable
fun BacklogItem(
    item: TodoItemModel,
    index: Int = -1,
    isActive: Boolean = false,
    onClickBtnTodoSettings: (Int) -> Unit = {},
    onClearActiveItem: () -> Unit = {},
    onTodoItemModified: (Long, String) -> Unit = {_,_ ->}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Gray95)
            .padding(bottom = 16.dp)
            .padding(start = 16.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = item.content,
                    selection = TextRange(item.content.length)
                )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
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

            if (!item.isBookmark && item.dDay == null) Spacer(modifier = Modifier.height(16.dp)) else Spacer(modifier = Modifier.height(8.dp))

            if (isActive) {

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newTextFieldValue ->
                        textFieldValue = newTextFieldValue
                    },
                    textStyle = PoptatoTypo.mdRegular.copy(color = Gray00),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                textFieldValue = textFieldValue.copy(
                                    selection = TextRange(textFieldValue.text.length)
                                )
                            }
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            onClearActiveItem()
                            if (item.content != textFieldValue.text) onTodoItemModified(item.todoId, textFieldValue.text)
                        }
                    ),
                    cursorBrush = SolidColor(Gray00)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.content,
                        style = PoptatoTypo.mdRegular,
                        color = Gray00
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_three_dot),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(if (item.isBookmark || item.dDay != null) Alignment.Top else Alignment.CenterVertically)
                .padding(top = 16.dp)
                .clickable {
                    onClickBtnTodoSettings(index)
                }
        )
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun BacklogGuideItem(
    onClickYesterdayList: () -> Unit = {},
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Primary60)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = BACKLOG_YESTERDAY_TASK_GUIDE,
            style = PoptatoTypo.smMedium,
            color = Gray100,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = interactionSource,
                    onClick = { onClickYesterdayList() }
                )

        ) {
            Text(
                text = CONFIRM_ACTION,
                style = PoptatoTypo.smSemiBold,
                color = Gray100
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_right_arrow),
                contentDescription = "",
                tint = Color.Unspecified
            )
        }
    }
}