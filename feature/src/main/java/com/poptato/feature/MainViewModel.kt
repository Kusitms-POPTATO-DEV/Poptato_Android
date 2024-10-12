package com.poptato.feature

import com.poptato.core.enums.BottomNavType
import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.navigation.NavRoutes
import com.poptato.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : BaseViewModel<MainPageState>(MainPageState()) {

    fun setBottomNavType(route: String?) {
        val type = when (route) {
            NavRoutes.BacklogScreen.route -> {
                BottomNavType.BACK_LOG
            }
            NavRoutes.TodayScreen.route -> {
                BottomNavType.TODAY
            }
            else -> {
                BottomNavType.DEFAULT
            }
        }
        updateBottomNav(type)
    }

    private fun updateBottomNav(type: BottomNavType) {
        updateState(
            uiState.value.copy(
                bottomNavType = type
            )
        )
    }

    fun onSelectedTodoItem(item: TodoItemModel) {
        updateState(
            uiState.value.copy(
                selectedTodoItem = item
            )
        )
        emitEventFlow(MainEvent.ShowTodoBottomSheet)
    }

    fun onUpdatedDeadline(date: String) {
        val updatedItem = uiState.value.selectedTodoItem.copy(deadline = date)

        updateState(
            uiState.value.copy(
                selectedTodoItem = updatedItem
            )
        )
    }
}