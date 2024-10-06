package com.poptato.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kakao.sdk.user.UserApiClient
import com.poptato.design_system.BtnKaKaoLoginText
import com.poptato.design_system.Gray100
import com.poptato.design_system.KaKaoMain
import com.poptato.design_system.PoptatoTypo
import com.poptato.design_system.R
import timber.log.Timber

@Composable
fun KaKaoLoginScreen(

) {
    val viewModel: KaKaoLoginViewModel = hiltViewModel()

    KaKaoLoginContent()
}

@Composable
fun KaKaoLoginContent() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_temp_splash),
            contentDescription = "ic_temp"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
                    .background(KaKaoMain, shape = RoundedCornerShape(8.dp))
                    .clickable { signInKakao(context) },
                contentAlignment = Alignment.Center
            ) {
                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kakao),
                        contentDescription = "ic_kakao"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = BtnKaKaoLoginText,
                        style = PoptatoTypo.mdMedium,
                        color = Gray100
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun signInKakao(context: Context) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        signInKakaoApp(context)
    } else {
        signInKakaoEmail(context)
    }
}

private fun signInKakaoApp(context: Context) {
    UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
        if (error != null) {
            Timber.tag("KaKao Login Error").e(error.stackTraceToString())
            return@loginWithKakaoTalk
        }
        Toast.makeText(context, "로그인 성공하였습니다.", Toast.LENGTH_SHORT).show()
//        login(token)
    }
}

private fun signInKakaoEmail(context: Context) {
    UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
        if (error != null) {
            Timber.tag("KaKao Login Error").e(error.stackTraceToString())
            return@loginWithKakaoAccount
        }

        Toast.makeText(context, "로그인 성공하였습니다.", Toast.LENGTH_SHORT).show()
//        login(token)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewKaKaoLogin() {
    KaKaoLoginContent()
}