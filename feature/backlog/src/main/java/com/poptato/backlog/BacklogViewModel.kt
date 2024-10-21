package com.poptato.backlog

import androidx.lifecycle.viewModelScope
import com.poptato.core.enums.TodoType
import com.poptato.core.util.TimeFormatter
import com.poptato.core.util.move
import com.poptato.domain.model.request.ListRequestModel
import com.poptato.domain.model.request.backlog.CreateBacklogRequestModel
import com.poptato.domain.model.request.backlog.GetBacklogListRequestModel
import com.poptato.domain.model.request.todo.TodoIdModel
import com.poptato.domain.model.request.todo.DeadlineContentModel
import com.poptato.domain.model.request.todo.DragDropRequestModel
import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import com.poptato.domain.model.request.todo.UpdateDeadlineRequestModel
import com.poptato.domain.model.response.backlog.BacklogListModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.domain.model.response.yesterday.YesterdayListModel
import com.poptato.domain.usecase.backlog.CreateBacklogUseCase
import com.poptato.domain.usecase.backlog.GetBacklogListUseCase
import com.poptato.domain.usecase.yesterday.GetYesterdayListUseCase
import com.poptato.domain.usecase.todo.DeleteTodoUseCase
import com.poptato.domain.usecase.todo.DragDropUseCase
import com.poptato.domain.usecase.todo.ModifyTodoUseCase
import com.poptato.domain.usecase.todo.SwipeTodoUseCase
import com.poptato.domain.usecase.todo.UpdateBookmarkUseCase
import com.poptato.domain.usecase.todo.UpdateDeadlineUseCase
import com.poptato.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BacklogViewModel @Inject constructor(
    private val createBacklogUseCase: CreateBacklogUseCase,
    private val getBacklogListUseCase: GetBacklogListUseCase,
    private val getYesterdayListUseCase: GetYesterdayListUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase,
    private val modifyTodoUseCase: ModifyTodoUseCase,
    private val dragDropUseCase: DragDropUseCase,
    private val updateDeadlineUseCase: UpdateDeadlineUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val swipeTodoUseCase: SwipeTodoUseCase
) : BaseViewModel<BacklogPageState>(
    BacklogPageState()
) {
    private var snapshotList: List<TodoItemModel> = emptyList()

    init {
        getYesterdayList(0, 1)
        getBacklogList(0, 100)
    }

    private fun getBacklogList(page: Int, size: Int) {
        viewModelScope.launch {
            getBacklogListUseCase.invoke(request = GetBacklogListRequestModel(page = page, size = size)).collect {
                resultResponse(it, ::onSuccessGetBacklogList)
            }
        }
    }

    private fun onSuccessGetBacklogList(response: BacklogListModel) {
        snapshotList = response.backlogs

        updateState(
            uiState.value.copy(
                backlogList = response.backlogs,
                totalPageCount = response.totalPageCount,
                totalItemCount = response.totalCount
            )
        )
    }

    private fun getYesterdayList(page: Int, size: Int) {
        viewModelScope.launch {
            getYesterdayListUseCase(request = ListRequestModel(page = page, size = size)).collect {
                resultResponse(it, { data ->
                    checkYesterdayListEmpty(data)
                    Timber.d("[어제 한 일] 서버통신 성공(Backlog) -> $data")
                }, { error ->
                    Timber.d("[어제 한 일] 서버통신 실패(Backlog) -> $error")
                })
            }
        }
    }

    private fun checkYesterdayListEmpty(response: YesterdayListModel) {
        updateState(
            uiState.value.copy(
                isYesterdayListEmpty = response.yesterdays.isEmpty()
            )
        )
    }

    fun onValueChange(newValue: String) {
        updateState(
            uiState.value.copy(
                taskInput = newValue
            )
        )
    }

    fun createBacklog(content: String) {
        val newList = uiState.value.backlogList.toMutableList()
        newList.add(0, TodoItemModel(content = content, todoId = Random.nextLong()))
        updateNewItemFlag(true)
        updateList(newList)

        viewModelScope.launch(Dispatchers.IO) {
            createBacklogUseCase.invoke(request = CreateBacklogRequestModel(content)).collect {
                resultResponse(it, { onSuccessCreateBacklog() }, { onFailedUpdateBacklogList() })
            }
        }
    }

    private fun onSuccessCreateBacklog() {
        getBacklogList(page = 0, size = uiState.value.backlogList.size)
    }

    private fun onSuccessUpdateBacklogList() {
        snapshotList = uiState.value.backlogList
    }

    private fun onFailedUpdateBacklogList() {
        updateList(snapshotList)
        emitEventFlow(BacklogEvent.OnFailedUpdateBacklogList)
    }

    fun swipeBacklogItem(item: TodoItemModel) {
        val newList = uiState.value.backlogList.filter { it.todoId != item.todoId }

        updateList(newList)

        Timber.i(item.todoId.toString())

        viewModelScope.launch {
            swipeTodoUseCase.invoke(TodoIdModel(item.todoId)).collect {
                resultResponse(it, { onSuccessUpdateBacklogList() }, { onFailedUpdateBacklogList() })
            }
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.backlogList.toMutableList()
        val safeToIndex = toIndex.coerceIn(0, currentList.size - 1)
        val safeFromIndex = fromIndex.coerceIn(0, currentList.size - 1)

        if (safeFromIndex != safeToIndex) {
            currentList.move(safeFromIndex, safeToIndex)
            updateList(currentList)
        }

        val todoIdList = currentList.map { it.todoId }

        viewModelScope.launch {
            dragDropUseCase.invoke(
                request = DragDropRequestModel(
                    type = TodoType.BACKLOG,
                    todoIds = todoIdList
                )
            ).collect {
                resultResponse(it, { onSuccessUpdateBacklogList() }, { onFailedUpdateBacklogList() })
            }
        }
    }

    private fun updateList(updatedList: List<TodoItemModel>) {
        updateState(
            uiState.value.copy(
                backlogList = updatedList
            )
        )
    }

    fun setDeadline(deadline: String?) {
        val dDay = TimeFormatter.calculateDDay(deadline)
        val updatedItem = uiState.value.selectedItem.copy(deadline = deadline ?: "", dDay = dDay)
        val newList = uiState.value.backlogList.map {
            if (it.todoId == updatedItem.todoId) updatedItem
            else it
        }

        updateState(
            uiState.value.copy(
                backlogList = newList,
                selectedItem = updatedItem
            )
        )

        viewModelScope.launch {
            updateDeadlineUseCase.invoke(
                request = UpdateDeadlineRequestModel(
                    todoId = uiState.value.selectedItem.todoId,
                    deadline = DeadlineContentModel(deadline)
                )
            ).collect {
                resultResponse(it, { onSuccessUpdateBacklogList() }, { onFailedUpdateBacklogList() })
            }
        }
    }

    fun onSelectedItem(item: TodoItemModel) {
        updateState(
            uiState.value.copy(
                selectedItem = item
            )
        )
    }

    fun deleteBacklog(id: Long) {
        val newList = uiState.value.backlogList.filter { it.todoId != id }
        updateList(newList)

        viewModelScope.launch {
            deleteTodoUseCase.invoke(id).collect {
                resultResponse(
                    it,
                    {
                        onSuccessUpdateBacklogList()
                        emitEventFlow(BacklogEvent.OnSuccessDeleteBacklog)
                    },
                    { onFailedUpdateBacklogList() }
                )
            }
        }
    }

    fun modifyTodo(item: ModifyTodoRequestModel) {
        val newList = uiState.value.backlogList.map {
            if (it.todoId == item.todoId) {
                it.copy(content = item.content.content)
            } else {
                it
            }
        }

        updateList(newList)

        viewModelScope.launch {
            modifyTodoUseCase.invoke(
                request = item
            ).collect {
                resultResponse(it, { onSuccessUpdateBacklogList() }, { onFailedUpdateBacklogList() })
            }
        }
    }

    fun updateNewItemFlag(flag: Boolean) {
        updateState(
            uiState.value.copy(
                isNewItemCreated = flag
            )
        )
    }

    fun updateBookmark(id: Long) {
        val newList = uiState.value.backlogList.map {
            if (it.todoId == id) {
                it.copy(isBookmark = !it.isBookmark)
            } else {
                it
            }
        }
        updateList(newList)

        viewModelScope.launch {
            updateBookmarkUseCase.invoke(id).collect {
                resultResponse(it, { onSuccessUpdateBacklogList() }, { onFailedUpdateBacklogList() })
            }
        }
    }
}