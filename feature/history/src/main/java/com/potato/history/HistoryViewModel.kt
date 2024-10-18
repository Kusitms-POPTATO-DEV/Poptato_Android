package com.potato.history

import androidx.lifecycle.viewModelScope
import com.poptato.domain.model.request.backlog.GetBacklogListRequestModel
import com.poptato.domain.model.request.history.HistoryListRequestModel
import com.poptato.domain.model.response.backlog.BacklogListModel
import com.poptato.domain.model.response.history.HistoryItemModel
import com.poptato.domain.model.response.history.HistoryListModel
import com.poptato.domain.usecase.backlog.GetBacklogListUseCase
import com.poptato.domain.usecase.history.GetHistoryListUseCase
import com.poptato.ui.base.BaseViewModel
import com.potato.history.model.HistoryGroupedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections.copy
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryListUseCase: GetHistoryListUseCase
) : BaseViewModel<HistoryPageState>(HistoryPageState()) {

    init {
        getHistoryList()
    }

    fun getHistoryList() {
        if (!uiState.value.isLoadingMore && (uiState.value.currentPage == 0 || uiState.value.currentPage < uiState.value.totalPageCount)) {
            updateState(
                uiState.value.copy(
                    isLoadingMore = true
                )
            )
            if(uiState.value.currentPage!=0) {
                updateState(
                    uiState.value.copy(
                        lastItemDate = uiState.value.nowLastItemDate
                    )
                )
            }

            viewModelScope.launch {
                try {
                    getHistoryListUseCase.invoke(
                        request = HistoryListRequestModel(page = uiState.value.currentPage, size = uiState.value.pageSize)
                    ).collect { result ->
                        resultResponse(result, ::onSuccessGetHistoryList)
                    }
                } catch (e: Exception) {
                    Timber.e("Failed to load next page: $e")
                    updateState(
                        uiState.value.copy(
                            isLoadingMore = false
                        )
                    )
                }
            }
        }
    }

    private fun onSuccessGetHistoryList(response: HistoryListModel) {
        val newItems = response.histories

        val groupedItems = newItems.groupBy { it.date }.map { (date, items) ->
            HistoryGroupedItem(date, items)
        }

        val lastDate = groupedItems.lastOrNull()?.date ?: uiState.value.lastItemDate

        updateState(
            uiState.value.copy(
                historyList = uiState.value.historyList + groupedItems,
                isLoadingMore = false,
                totalPageCount = response.totalPageCount,
                currentPage = uiState.value.currentPage + 1,
                lastItemDate = if (uiState.value.currentPage == 0) lastDate else uiState.value.nowLastItemDate,
                nowLastItemDate = lastDate
            )
        )
    }

    fun updateRenderingComplete(isComplete: Boolean) {
        updateState (
            uiState.value.copy(isRenderingComplete = isComplete)
        )
    }
}
