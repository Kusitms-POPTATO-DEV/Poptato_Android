package com.poptato.backlog

import androidx.compose.animation.core.snap
import androidx.lifecycle.viewModelScope
import com.poptato.core.util.move
import com.poptato.domain.model.request.backlog.CreateBacklogRequestModel
import com.poptato.domain.model.request.backlog.GetBacklogListRequestModel
import com.poptato.domain.model.response.backlog.BacklogListModel
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.domain.usecase.backlog.CreateBacklogUseCase
import com.poptato.domain.usecase.backlog.GetBacklogListUseCase
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
    private val getBacklogListUseCase: GetBacklogListUseCase
) : BaseViewModel<BacklogPageState>(
    BacklogPageState()
) {
    private var snapshotList: List<TodoItemModel> = emptyList()

    init {
        getBacklogList(0, 8)
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
        updateState(
            uiState.value.copy(
                backlogList = newList
            )
        )

        viewModelScope.launch(Dispatchers.IO) {
            createBacklogUseCase.invoke(request = CreateBacklogRequestModel(content)).collect {
                resultResponse(it, { onSuccessCreateBacklog() }, { onFailedCreateBacklog() })
            }
        }
    }

    private fun onSuccessCreateBacklog() {
        snapshotList = uiState.value.backlogList
    }

    private fun onFailedCreateBacklog() {
        updateState(
            uiState.value.copy(
                backlogList = snapshotList
            )
        )

        emitEventFlow(BacklogEvent.OnFailedCreateBacklog)
    }

    fun removeBacklogItem(item: TodoItemModel) {
        val newList = uiState.value.backlogList.filter { it.todoId != item.todoId }

        updateState(
            uiState.value.copy(
                backlogList = newList
            )
        )
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.backlogList.toMutableList()
        currentList.move(fromIndex, toIndex)
        updateList(currentList)
    }

    private fun updateList(updatedList: List<TodoItemModel>) {
        updateState(
            uiState.value.copy(
                backlogList = updatedList
            )
        )
    }

    fun setDeadline(deadline: String) {
        val updatedItem = uiState.value.selectedItem.copy(deadline = deadline)
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
    }

    fun onSelectedItem(item: TodoItemModel) {
        updateState(
            uiState.value.copy(
                selectedItem = item
            )
        )
    }
}