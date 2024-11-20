package com.potato.history

import androidx.lifecycle.viewModelScope
import com.poptato.domain.model.request.backlog.GetBacklogListRequestModel
import com.poptato.domain.model.request.history.HistoryCalendarRequestModel
import com.poptato.domain.model.request.history.HistoryListRequestModel
import com.poptato.domain.model.response.backlog.BacklogListModel
import com.poptato.domain.model.response.history.HistoryCalendarListModel
import com.poptato.domain.model.response.history.HistoryItemModel
import com.poptato.domain.model.response.history.HistoryListModel
import com.poptato.domain.usecase.backlog.GetBacklogListUseCase
import com.poptato.domain.usecase.history.GetHistoryCalendarListUseCase
import com.poptato.domain.usecase.history.GetHistoryListUseCase
import com.poptato.history.R
import com.poptato.ui.base.BaseViewModel
import com.potato.history.model.HistoryGroupedItem
import com.potato.history.model.MonthNav
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import timber.log.Timber
import java.util.Collections.copy
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryListUseCase: GetHistoryListUseCase,
    private val getHistoryCalendarListUseCase: GetHistoryCalendarListUseCase
) : BaseViewModel<HistoryPageState>(HistoryPageState()) {

    init {
        getHistoryList()
        getCalendarList()
    }

    fun getHistoryList() {
        if (!uiState.value.isLoadingMore && (uiState.value.currentPage == 0 || uiState.value.currentPage < uiState.value.totalPageCount)) {
            updateState(
                uiState.value.copy(
                    isLoadingMore = true
                )
            )

            viewModelScope.launch {
                try {
                    getHistoryListUseCase.invoke(
                        request = HistoryListRequestModel(page = uiState.value.currentPage, size = uiState.value.pageSize, date = uiState.value.selectedDate)
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
        updateState(
            uiState.value.copy(
                historyList = newItems,
                isLoadingMore = false,
                totalPageCount = response.totalPageCount,
                currentPage = uiState.value.currentPage + 1
            )
        )
    }

    fun getImageResourceForDate(date: LocalDate, hasEvent: Boolean): Int {
        return when {
            hasEvent && date.isBefore(LocalDate.now()) -> com.poptato.design_system.R.drawable.ic_history_star
            date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now()) -> com.poptato.design_system.R.drawable.ic_history_circle
            else -> com.poptato.design_system.R.drawable.ic_history_moon
        }
    }

    fun getCalendarList(){
        viewModelScope.launch {
            try {
                getHistoryCalendarListUseCase.invoke(
                    request = HistoryCalendarRequestModel(
                        year = uiState.value.currentMonthStartDate.year.toString(),
                        month = uiState.value.currentMonthStartDate.monthValue)
                ).collect { result ->
                    resultResponse(result, ::onSuccessGetCalendarList)
                }
            } catch (e: Exception) {
                Timber.e("Failed to load history list: $e")
            }
        }
    }

    private fun onSuccessGetCalendarList(response: HistoryCalendarListModel){
        updateState(
            uiState.value.copy(
                eventDates = response.dates
            )
        )
    }

    fun updateSelectedDate(selectedDate: String) {
        updateState(
            uiState.value.copy(
                selectedDate = selectedDate,
                currentPage = 0,
                isLoadingMore = false,
            )
        )

        viewModelScope.launch {
            getHistoryList()
        }
    }

    fun updateCurrentMonth(dir: MonthNav) {
        val updatedMonthStartDate = when (dir) {
            MonthNav.PREVIOUS -> uiState.value.currentMonthStartDate.minusMonths(1).withDayOfMonth(1)
            MonthNav.NEXT -> uiState.value.currentMonthStartDate.plusMonths(1).withDayOfMonth(1)
        }

        updateState(
            uiState.value.copy(
                currentMonthStartDate = updatedMonthStartDate
            )
        )

        viewModelScope.launch {
            getCalendarList()
            getHistoryList()
        }
    }
}
