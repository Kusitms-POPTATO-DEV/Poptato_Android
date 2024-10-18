package com.poptato.backlog

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.poptato.design_system.BACKLOG_YESTERDAY_TASK_GUIDE
import com.poptato.design_system.Backlog
import com.poptato.design_system.BacklogHint
import com.poptato.design_system.CONFIRM_ACTION
import com.poptato.design_system.ERROR_CREATE_BACKLOG
import com.poptato.design_system.EmptyBacklogTitle
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray70
import com.poptato.design_system.Gray80
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import com.poptato.domain.model.request.todo.TodoContentModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.ui.common.TopBar
import com.poptato.ui.util.DragDropListState
import com.poptato.ui.util.rememberDragDropListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun BacklogScreen(
    goToYesterdayList: () -> Unit = {},
    showBottomSheet: (TodoItemModel) -> Unit = {},
    todoBottomSheetClosedFlow: SharedFlow<Unit>,
    updateDeadlineFlow: SharedFlow<String>,
    deleteTodoFlow: SharedFlow<Long>,
    activateItemFlow: SharedFlow<Long>
) {
    val viewModel: BacklogViewModel = hiltViewModel()
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val uiState: BacklogPageState by viewModel.uiState.collectAsStateWithLifecycle()
    val dragDropListState = rememberDragDropListState(
        lazyListState = rememberLazyListState(),
        onMove = { from, to ->
            viewModel.moveItem(from, to)
        }
    )
    var activeItemId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(activateItemFlow) {
        activateItemFlow.collect { id ->
            activeItemId = id
        }
    }

    LaunchedEffect(todoBottomSheetClosedFlow) {
        todoBottomSheetClosedFlow.collect {
            // TODO: 데이터 갱신 함수 호출
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
                    Toast.makeText(context, ERROR_CREATE_BACKLOG, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(updateDeadlineFlow) {
        updateDeadlineFlow.collect {
            viewModel.setDeadline(it)
        }
    }

    BacklogContent(
        uiState = uiState,
        onValueChange = { newValue -> viewModel.onValueChange(newValue) },
        createBacklog = { newItem -> viewModel.createBacklog(newItem) },
        onItemSwiped = { itemToRemove -> viewModel.removeBacklogItem(itemToRemove) },
        onClickYesterdayList = { goToYesterdayList() },      // TODO 테스트용: "어제 리스트 체크하기" 스낵바 생성 후 변경 예정
        dragDropListState = dragDropListState,
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
        resetNewItemFlag = { viewModel.updateNewItemFlag(false) }
    )
}

@Composable
fun BacklogContent(
    uiState: BacklogPageState = BacklogPageState(),
    onValueChange: (String) -> Unit = {},
    createBacklog: (String) -> Unit = {},
    onClickYesterdayList: () -> Unit = {},
    onItemSwiped: (TodoItemModel) -> Unit = {},
    dragDropListState: DragDropListState? = null,
    onClickBtnTodoSettings: (Int) -> Unit = {},
    interactionSource: MutableInteractionSource,
    activeItemId: Long?,
    onClearActiveItem: () -> Unit = {},
    onTodoItemModified: (Long, String) -> Unit = {_,_ ->},
    resetNewItemFlag: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        TopBar(
            titleText = Backlog,
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
                        dragDropListState = dragDropListState!!,
                        onClickBtnTodoSettings = onClickBtnTodoSettings,
                        activeItemId = activeItemId,
                        onClearActiveItem = onClearActiveItem,
                        onTodoItemModified = onTodoItemModified,
                        isNewItemCreated = uiState.isNewItemCreated,
                        resetNewItemFlag = resetNewItemFlag
                    )
                }
            }

            BacklogGuideItem(
                onClickYesterdayList = onClickYesterdayList,
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                interactionSource = interactionSource
            )
        }
    }
}

@Composable
fun CreateBacklogTextFiled(
    taskInput: String = "",
    onValueChange: (String) -> Unit = {},
    createBacklog: (String) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Gray70, shape = RoundedCornerShape(8.dp))
    ) {
        BasicTextField(
            value = taskInput,
            onValueChange = { onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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

@Composable
fun BacklogTaskList(
    taskList: List<TodoItemModel> = emptyList(),
    onItemSwiped: (TodoItemModel) -> Unit = {},
    dragDropListState: DragDropListState,
    onClickBtnTodoSettings: (Int) -> Unit = {},
    activeItemId: Long?,
    onClearActiveItem: () -> Unit = {},
    onTodoItemModified: (Long, String) -> Unit = {_,_ ->},
    isNewItemCreated: Boolean = false,
    resetNewItemFlag: () -> Unit = {}
) {
    var draggedItem by remember { mutableStateOf<TodoItemModel?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        dragDropListState.onDragStart(offset)
                        draggedItem = taskList[dragDropListState.currentIndexOfDraggedItem
                            ?: return@detectDragGesturesAfterLongPress]
                        isDragging = true
                    },
                    onDragEnd = {
                        dragDropListState.onDragInterrupted()
                        draggedItem = null
                        isDragging = false
                    },
                    onDragCancel = {
                        dragDropListState.onDragInterrupted()
                        draggedItem = null
                        isDragging = false
                    },
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropListState.onDrag(offset)
                        if (dragDropListState.overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress
                        dragDropListState
                            .checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                dragDropListState.overscrollJob = scope.launch {
                                    val adjustedScroll = it * 0.5f
                                    dragDropListState.lazyListState.scrollBy(adjustedScroll)
                                }
                            } ?: run { dragDropListState.overscrollJob?.cancel() }
                    }
                )
            },
        state = dragDropListState.lazyListState
    ) {
        itemsIndexed(taskList, key = { _, item -> item.todoId }) { index, item ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val isDragged = index == dragDropListState.currentIndexOfDraggedItem
            val isActive = activeItemId == item.todoId

            Box(
                modifier = Modifier
                    .zIndex(if (index == dragDropListState.currentIndexOfDraggedItem) 1f else 0f)
                    .graphicsLayer {
                        translationY =
                            dragDropListState.elementDisplacement.takeIf { index == dragDropListState.currentIndexOfDraggedItem }
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
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn(),
                    exit = fadeOut()
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
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item { Spacer(modifier = Modifier.height(45.dp)) }
    }

    LaunchedEffect(taskList.size, isDragging) {
        if (dragDropListState.lazyListState.firstVisibleItemIndex != 0 && !isDragging && isNewItemCreated) {
            dragDropListState.lazyListState.scrollToItem(0)
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
            .padding(vertical = 16.dp)
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
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            textFieldValue = textFieldValue.copy(
                                selection = TextRange(textFieldValue.text.length)
                            )
                        }
                    },
                singleLine = true,
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
            Text(
                text = item.content,
                color = Gray00,
                style = PoptatoTypo.mdRegular,
                modifier = Modifier
                    .weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_three_dot),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
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