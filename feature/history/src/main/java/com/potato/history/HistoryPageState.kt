package com.potato.history

import com.poptato.ui.base.PageState
import com.potato.history.model.HistoryGroupedItem
import com.potato.history.model.HistoryItemModel

data class HistoryPageState(
    val historyList: List<HistoryGroupedItem> = emptyList(),
    val totalPageCount: Int = -1,
    val lastItemDate: String = ""
) : PageState