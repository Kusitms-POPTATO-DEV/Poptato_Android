package com.potato.history.model

import com.poptato.domain.model.response.history.HistoryItemModel

data class HistoryGroupedItem(
    val date: String,
    val items: List<HistoryItemModel>
)