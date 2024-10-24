package com.poptato.yesterdaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray40
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R
import com.poptato.design_system.YesterdayAllCheckBtn
import com.poptato.design_system.YesterdayListTitle
import com.poptato.domain.model.enums.TodoStatus
import com.poptato.domain.model.response.yesterday.YesterdayItemModel
import com.poptato.ui.common.PoptatoCheckBox
import timber.log.Timber

@Composable
fun YesterdayListScreen(
    goBackToBacklog: () -> Unit = {},
    showAllCheckPage: () -> Unit = {}
) {

    val viewModel: YesterdayListViewModel = hiltViewModel()
    val uiState: YesterdayListPageState by viewModel.uiState.collectAsStateWithLifecycle()

    YesterdayContent(
        uiState = uiState,
        onClickCloseBtn = { goBackToBacklog() },
        onCheckedChange = { id, status ->
            viewModel.onCheckedTodo(id, status)
        },
        onClickAllCheckBtn = {
            viewModel.onCheckAllTodoList()
            showAllCheckPage()
        }
    )
}

@Composable
fun YesterdayContent(
    uiState: YesterdayListPageState = YesterdayListPageState(),
    onClickCloseBtn: () -> Unit = {},
    onCheckedChange: (Long, TodoStatus) -> Unit = {_, _ ->},
    onClickAllCheckBtn: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TitleTopBar(
                onClickCloseBtn = onClickCloseBtn
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                YesterdayTodoList(
                    list = uiState.yesterdayList,
                    onCheckedChange = onCheckedChange
                )
            }

            AllCheckBtn(
                onClickAllCheckBtn = onClickAllCheckBtn
            )
        }

    }
}

@Composable
fun TitleTopBar(
    onClickCloseBtn: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = YesterdayListTitle,
            style = PoptatoTypo.mdMedium,
            color = Gray00,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.Center)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_close_no_bg),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 24.dp, height = 24.dp)
                .clickable { onClickCloseBtn() }
        )
    }
}

@Composable
fun YesterdayTodoList(
    list: List<YesterdayItemModel> = emptyList(),
    onCheckedChange: (Long, TodoStatus) -> Unit = {_, _ ->},
) {
    Timber.d("[어제 한 일] list -> $list")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        items(list, key = { it.todoId }) { item ->
            Spacer(modifier = Modifier.height(8.dp))

            YesterdayTodoItem(
                item = item,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun YesterdayTodoItem(
    item: YesterdayItemModel = YesterdayItemModel(),
    onCheckedChange: (Long, TodoStatus) -> Unit = {_, _ ->},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Gray95)
            .padding(vertical = 16.dp)
            .padding(start = 18.dp, end = 16.dp)
    ) {
        PoptatoCheckBox(
            isChecked = item.todoStatus == TodoStatus.COMPLETED,
            onCheckedChange = { onCheckedChange(item.todoId, item.todoStatus) }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = item.content,
            color = Gray40,
            style = PoptatoTypo.mdRegular,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_three_dot),
            contentDescription = "",
            tint = Color.Unspecified
        )
    }
}

@Composable
fun AllCheckBtn(
    onClickAllCheckBtn: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .background(Primary60, shape = RoundedCornerShape(12.dp))
            .clickable { onClickAllCheckBtn() }
    ) {
        Text(
            text = YesterdayAllCheckBtn,
            style = PoptatoTypo.lgSemiBold,
            color = Gray100,
            modifier = Modifier
                .padding(vertical = 15.dp)
                .align(Alignment.Center)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun previewYesterdayList() {
    YesterdayContent()
}