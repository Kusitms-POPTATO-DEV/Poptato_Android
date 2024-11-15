package com.poptato.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.poptato.design_system.Gray60
import com.poptato.design_system.Gray80
import com.poptato.design_system.Gray90
import com.poptato.design_system.PoptatoTypo
import com.poptato.domain.model.response.category.CategoryIconTotalListModel
import com.poptato.domain.model.response.category.CategoryIconTypeListModel

@Composable
fun CategoryBottomSheet(
    categoryIconList: CategoryIconTotalListModel = CategoryIconTotalListModel()
) {

    CategoryBottomSheetContent(categoryIconList)
}

@Composable
fun CategoryBottomSheetContent(
    categoryIconList: CategoryIconTotalListModel = CategoryIconTotalListModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(610.dp)
            .background(Gray90)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .height(4.dp)
                .width(64.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Gray80)
                .align(Alignment.CenterHorizontally)
        )

        CategoryIconListTotal(
            categoryIconList = categoryIconList
        )
    }
}

@Composable
fun CategoryIconListTotal(
    categoryIconList: CategoryIconTotalListModel = CategoryIconTotalListModel(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(categoryIconList.icons, key = { it.iconType }) { iconTypes ->
            CategoryIconListForType(iconTypes)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryIconListForType(
    categoryIcons: CategoryIconTypeListModel = CategoryIconTypeListModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = categoryIcons.iconType,
            style = PoptatoTypo.smMedium,
            color = Gray60
        )

        FlowRow(
            maxItemsInEachRow = 7,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            categoryIcons.icons.forEach { item ->
                Image(painter = painterResource(id = com.poptato.design_system.R.drawable.ic_history_calendar),
                    contentDescription = "",
                    modifier = Modifier
                        .size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewCategoryBottomSheet() {
    CategoryBottomSheetContent()
}