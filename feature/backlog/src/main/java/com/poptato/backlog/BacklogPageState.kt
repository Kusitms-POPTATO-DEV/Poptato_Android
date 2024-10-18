package com.poptato.backlog

import com.poptato.domain.model.response.today.TodoItemModel
import com.poptato.ui.base.PageState

data class BacklogPageState(
    val backlogList: List<TodoItemModel> = emptyList(),
    val taskInput: String = "",
    val showTodoBottomSheet: Boolean = false,
    val selectedItem: TodoItemModel = TodoItemModel(),
    val totalItemCount: Int = -1,
    val totalPageCount: Int = -1,
    val isYesterdayListEmpty: Boolean = true
): PageState