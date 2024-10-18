package com.poptato.data.model.response.yesterday

import com.google.gson.annotations.SerializedName

data class YesterdayListItemResponse (
    @SerializedName("todoId")
    val todoId: Long = -1,
    @SerializedName("content")
    val content: String = ""
)