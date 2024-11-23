package com.poptato.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.poptato.design_system.BOOKMARK
import com.poptato.design_system.Gray90
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.Primary60
import com.poptato.design_system.R

@Composable
fun BookmarkItem() {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(4.dp))
            .background(Gray90),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_star_filled),
            contentDescription = "",
            modifier = Modifier
                .padding(start = 4.dp)
                .padding(vertical = 4.dp)
                .size(12.dp),
            tint = Primary60
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = BOOKMARK,
            style = PoptatoTypo.xsSemiBold,
            color = Primary60,
            modifier = Modifier
                .padding(end = 4.dp),
        )
    }
}