package com.potato.history

import com.poptato.ui.base.PageState
import com.potato.history.model.HistoryGroupedItem
import com.potato.history.model.HistoryItemModel

data class HistoryPageState(
    val historyList: List<HistoryGroupedItem> = emptyList(),
    val totalPageCount: Int = -1,
    val lastItemDate: String = "",
    val nowLastItemDate: String = "",
    val isLoadingMore: Boolean = false,
    var currentPage: Int = 0,
    val pageSize: Int = 15,
    val isRenderingComplete: Boolean = false
) : PageState