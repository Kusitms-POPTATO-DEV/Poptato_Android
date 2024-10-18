package com.potato.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray20
import com.poptato.design_system.Gray70
import com.poptato.design_system.Gray80
import com.poptato.design_system.Gray95
import com.poptato.design_system.HistoryListEmpty
import com.poptato.design_system.HistoryTitle
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.R
import com.poptato.domain.model.response.history.HistoryItemModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun HistoryScreen(

) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val uiState: HistoryPageState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryContent(
        uiState = uiState,
        onLoadNextPage = { viewModel.getHistoryList() },
        isRendering = {viewModel.updateRenderingComplete(true)},
        clearRendering = {viewModel.updateRenderingComplete(false)}
    )
}

@Composable
fun HistoryContent(
    uiState: HistoryPageState = HistoryPageState(),
    onLoadNextPage: () -> Unit,
    isRendering: () -> Unit,
    clearRendering: () -> Unit,
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = HistoryListEmpty,
                    color = Gray80,
                    style = PoptatoTypo.lgMedium
                )
            }
        } else {
            Text(
                text = HistoryTitle,
                color = Gray00,
                style = PoptatoTypo.xLSemiBold,
                modifier = Modifier
                    .background(Gray100)
                    .padding(start = 16.dp, top = 13.dp, bottom = 13.dp)
            )
            InfinityLazyColumn(
                state = listState,
                loadMore = onLoadNextPage
            ) {

                items(uiState.historyList) { groupedItem ->
                    val isFirstOfDate = uiState.historyList.indexOfFirst { it.date == groupedItem.date } == uiState.historyList.indexOf(groupedItem)

                    if (isFirstOfDate) {
                        DateHeader(date = groupedItem.date)
                    }

                    groupedItem.items.forEach { item ->
                        HistoryListItem(item = item)
                    }

                    if(groupedItem.date != uiState.lastItemDate || !isFirstOfDate){
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (groupedItem.date == uiState.nowLastItemDate && !uiState.isRenderingComplete) {
                        isRendering()
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow { uiState.historyList }
                    .collect {
                        if (uiState.isRenderingComplete) {
                            onLoadNextPage()
                            clearRendering()
                        }
                    }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = date.replace("-", "."),
            color = Gray70,
            style = PoptatoTypo.xsRegular,
            modifier = Modifier
                .padding(end = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth(),
            thickness = 1.dp,
            color = Gray95
        )
    }

}

@Composable
fun HistoryListItem(item: HistoryItemModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
            .background(Color.Unspecified, shape = MaterialTheme.shapes.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_history_checked),
            contentDescription = "Check",
            tint =  Color.Unspecified,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = item.content,
            color = Gray00,
            style = PoptatoTypo.smMedium,
            modifier = Modifier.weight(1f)
        )
    }
}


private fun LazyListState.reachedLastItem(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index == layoutInfo.totalItemsCount - 1
}

@SuppressLint("ComposableNaming")
@Composable
private fun LazyListState.onLoadMoreWhenLastItemVisible(action: () -> Unit) {
    val reached by remember {
        derivedStateOf {
            reachedLastItem()
        }
    }
    LaunchedEffect(reached) {
        if (reached) action()
    }
}

@Composable
fun InfinityLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    loadMore: () -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    state.onLoadMoreWhenLastItemVisible(action = loadMore)

    LazyColumn(
        modifier = modifier,
        state = state,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}