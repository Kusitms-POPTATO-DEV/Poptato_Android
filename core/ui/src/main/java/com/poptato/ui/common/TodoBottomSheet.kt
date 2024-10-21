package com.poptato.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poptato.design_system.Danger50
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray40
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.R
import com.poptato.design_system.delete
import com.poptato.design_system.dueDate
import com.poptato.design_system.modify
import com.poptato.domain.model.response.today.TodoItemModel

@Composable
fun TodoBottomSheet(
    item: TodoItemModel = TodoItemModel(),
    setDeadline: (String?) -> Unit = {},
    onClickShowDatePicker: () -> Unit = {},
    onClickBtnDelete: (Long) -> Unit = {},
    onClickBtnModify: (Long) -> Unit = {},
    onClickBtnBookmark: (Long) -> Unit = {}
) {
    var deadline by remember { mutableStateOf(item.deadline) }
    var isBookmark by remember { mutableStateOf(item.isBookmark) }

    LaunchedEffect(item) {
        deadline = item.deadline
        isBookmark = item.isBookmark
    }

    TodoBottomSheetContent(
        item = item.copy(deadline = deadline, isBookmark = isBookmark),
        removeDeadline = {
            deadline = ""
            setDeadline(null)
        },
        onClickShowDatePicker = onClickShowDatePicker,
        onClickBtnDelete = onClickBtnDelete,
        onClickBtnModify = onClickBtnModify,
        onClickBtnBookmark = {
            onClickBtnBookmark(it)
            isBookmark = !isBookmark
        }
    )
}

@Composable
fun TodoBottomSheetContent(
    item: TodoItemModel = TodoItemModel(),
    removeDeadline: () -> Unit = {},
    onClickShowDatePicker: () -> Unit = {},
    onClickBtnDelete: (Long) -> Unit = {},
    onClickBtnModify: (Long) -> Unit = {},
    onClickBtnBookmark: (Long) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(324.dp)
            .background(Gray100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray100)
                .padding(top = 24.dp)
                .padding(horizontal = 24.dp)
        ) {

            Text(
                text = item.content,
                style = PoptatoTypo.xLMedium,
                color = Gray00,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            if (item.isBookmark) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star_filled),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable { onClickBtnBookmark(item.todoId) }
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star_empty),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable { onClickBtnBookmark(item.todoId) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            BottomSheetButton(
                text = modify,
                buttonColor = Gray95,
                textColor = Gray40,
                modifier = Modifier.weight(1f),
                onClickBtn = { onClickBtnModify(item.todoId) }
            )

            Spacer(modifier = Modifier.width(16.dp))

            BottomSheetButton(
                text = delete,
                buttonColor = Gray100,
                textColor = Danger50,
                modifier = Modifier.weight(1f),
                onClickBtn = { onClickBtnDelete(item.todoId) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .background(Gray95)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (item.deadline.isEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_btn_add),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .clickable { onClickShowDatePicker() }
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_btn_minus),
                        contentDescription = "",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .clickable { removeDeadline() }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = dueDate,
                    color = Gray40,
                    style = PoptatoTypo.mdMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.deadline,
                    color = Gray40,
                    style = PoptatoTypo.mdMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .background(Gray95)
            )
        }
    }
}

@SuppressLint("ModifierParameter")
@Composable
fun BottomSheetButton(
    text: String = "",
    buttonColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClickBtn: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(148f / 40f)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Gray95, shape = RoundedCornerShape(8.dp))
            .background(buttonColor)
            .clickable { onClickBtn() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = PoptatoTypo.mdMedium
        )
    }
}