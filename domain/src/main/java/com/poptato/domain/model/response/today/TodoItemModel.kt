package com.poptato.domain.model.response.today

import com.poptato.domain.model.enums.TodoStatus

data class TodoItemModel(
    val todoId: Long = -1,
    val content: String = "",
    val todoStatus: TodoStatus = TodoStatus.INCOMPLETE,
    val isBookmark: Boolean = false,
    val deadline: String = "",
    val dDay: Int = -1
)
