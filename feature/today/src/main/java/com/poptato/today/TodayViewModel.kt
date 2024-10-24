package com.poptato.today

import androidx.lifecycle.viewModelScope
import com.poptato.core.enums.TodoType
import com.poptato.core.util.TimeFormatter
import com.poptato.core.util.move
import com.poptato.domain.model.enums.TodoStatus
import com.poptato.domain.model.request.today.GetTodayListRequestModel
import com.poptato.domain.model.request.todo.DragDropRequestModel
import com.poptato.domain.model.request.todo.TodoIdModel
import com.poptato.domain.model.response.today.TodayListModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.domain.usecase.today.GetTodayListUseCase
import com.poptato.domain.usecase.todo.DragDropUseCase
import com.poptato.domain.usecase.todo.SwipeTodoUseCase
import com.poptato.domain.usecase.todo.UpdateTodoCompletionUseCase
import com.poptato.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayListUseCase: GetTodayListUseCase,
    private val updateTodoCompletionUseCase: UpdateTodoCompletionUseCase,
    private val swipeTodoUseCase: SwipeTodoUseCase,
    private val dragDropUseCase: DragDropUseCase,
) : BaseViewModel<TodayPageState>(TodayPageState()) {
    private var snapshotList: List<TodoItemModel> = emptyList()

    init {
        getTodayList(0, 50)
    }

    fun onCheckedTodo(status: TodoStatus, id: Long) {
        val newStatus = if(status == TodoStatus.COMPLETED) TodoStatus.INCOMPLETE else TodoStatus.COMPLETED
        val selectedItem = uiState.value.todayList.find { it.todoId == id }?.copy(todoStatus = newStatus)
        val remainingItems = uiState.value.todayList.filter { it.todoId != id }
        val newTodays = if (selectedItem != null) {
            val incompleteItems = remainingItems.filter { it.todoStatus == TodoStatus.INCOMPLETE }.toMutableList()
            val completeItems = remainingItems.filter { it.todoStatus == TodoStatus.COMPLETED }.toMutableList()

            if (selectedItem.todoStatus == TodoStatus.COMPLETED) {
                completeItems.add(selectedItem)
            } else {
                incompleteItems.add(selectedItem)
            }

            incompleteItems + completeItems
        } else {
            remainingItems
        }

        updateList(newTodays)

        viewModelScope.launch {
            updateTodoCompletionUseCase.invoke(id).collect {
                resultResponse(it, { onSuccessUpdateTodayList() }, { onFailedUpdateTodayList() })
            }
        }
    }

    private fun getTodayList(page: Int, size: Int) {
        viewModelScope.launch {
            getTodayListUseCase.invoke(request = GetTodayListRequestModel(page = page, size = size)).collect {
                resultResponse(it, ::onSuccessGetTodayList, { onFailedUpdateTodayList() })
            }
        }
    }

    private fun onSuccessGetTodayList(response: TodayListModel) {
        snapshotList = response.todays

        updateState(
            uiState.value.copy(
                todayList = response.todays,
                totalPageCount = response.totalPageCount,
                isFinishedInitialization = true
            )
        )
    }

    private fun onSuccessUpdateTodayList() {
        snapshotList = uiState.value.todayList
    }

    private fun onFailedUpdateTodayList() {
        updateList(snapshotList)
        emitEventFlow(TodayEvent.OnFailedUpdateTodayList)
    }

    private fun updateList(newList: List<TodoItemModel>) {
        updateState(
            uiState.value.copy(
                todayList = newList
            )
        )
    }

    fun swipeTodayItem(item: TodoItemModel) {
        val newList = uiState.value.todayList.filter { it.todoId != item.todoId }

        updateList(newList)

        Timber.i(item.todoId.toString())

        viewModelScope.launch {
            swipeTodoUseCase.invoke(TodoIdModel(item.todoId)).collect {
                resultResponse(it, { onSuccessUpdateTodayList() }, { onFailedUpdateTodayList() })
            }
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.todayList.toMutableList()
        val lastIncompleteIndex = currentList.indexOfLast { it.todoStatus == TodoStatus.INCOMPLETE }
        var safeToIndex = toIndex

        if (lastIncompleteIndex in fromIndex..<toIndex) {
            safeToIndex = lastIncompleteIndex
        } else if (lastIncompleteIndex in toIndex..<fromIndex) {
            safeToIndex = lastIncompleteIndex + 1
        }

        if (fromIndex != safeToIndex) {
            currentList.move(fromIndex, safeToIndex)
            updateList(currentList)
        }

        val todoIdList = uiState.value.todayList
            .filter { it.todoStatus == TodoStatus.INCOMPLETE }
            .map { it.todoId }

        viewModelScope.launch {
            dragDropUseCase.invoke(
                request = DragDropRequestModel(
                    type = TodoType.TODAY,
                    todoIds = todoIdList
                )
            ).collect {
                resultResponse(it, { onSuccessUpdateTodayList() }, { onFailedUpdateTodayList() })
            }
        }
    }
}