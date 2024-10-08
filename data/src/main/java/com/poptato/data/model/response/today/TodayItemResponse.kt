package com.poptato.data.model.response.today

import com.google.gson.annotations.SerializedName
import com.poptato.domain.model.enums.TodoStatus

data class TodayItemResponse(
    @SerializedName("todoId")
    val todoId: Long = -1,
    @SerializedName("content")
    val content: String = "",
    @SerializedName("todoStatus")
    val todoStatus: TodoStatus = TodoStatus.INCOMPLETE,
    @SerializedName("isComplete")
    val isComplete: Boolean = false
)