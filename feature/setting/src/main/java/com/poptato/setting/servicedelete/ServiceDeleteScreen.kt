package com.poptato.setting.servicedelete

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.poptato.design_system.Danger50
import com.poptato.design_system.FirstNoticeContent
import com.poptato.design_system.FirstNoticeTitle
import com.poptato.design_system.Gray00
import com.poptato.design_system.Gray100
import com.poptato.design_system.Gray40
import com.poptato.design_system.Gray95
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.R
import com.poptato.design_system.SecondNoticeContent
import com.poptato.design_system.SecondNoticeTitle
import com.poptato.design_system.UserDeleteBtn
import com.poptato.design_system.UserDeleteTitle

@Composable
fun ServiceDeleteScreen() {

    ServiceDeleteContent()
}

@Composable
fun ServiceDeleteContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {

        CloseBtn()

        Text(
            text = UserDeleteTitle,
            color = Gray00,
            style = PoptatoTypo.xxLSemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(top = 32.dp)
        ) {
            DeleteNotice()
        }

        UserDeleteBtn()
    }
}

@Composable
fun CloseBtn() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(end = 16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_close_no_bg),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
fun DeleteNotice() {
    Column {
        DeleteNoticeItem(noticeTitle = FirstNoticeTitle, noticeContent = FirstNoticeContent)
        Spacer(modifier = Modifier.height(16.dp))
        DeleteNoticeItem(noticeTitle = SecondNoticeTitle, noticeContent = SecondNoticeContent)
    }
}

@Composable
fun DeleteNoticeItem(
    noticeTitle: String,
    noticeContent: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 17.dp, end = 15.dp)
            .background(Gray95, shape = RoundedCornerShape(12.dp))
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_caution),
                contentDescription = "",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp))

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = noticeTitle,
                color = Gray00,
                style = PoptatoTypo.mdSemiBold
            )
        }

        Text(
            text = noticeContent,
            color = Gray40,
            style = PoptatoTypo.smMedium,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        )
    }
}

@Composable
fun UserDeleteBtn() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .background(Danger50, shape = RoundedCornerShape(12.dp))
    ) {
        Text(
            text = UserDeleteBtn,
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
fun PreviewSetting() {
    ServiceDeleteContent()
}