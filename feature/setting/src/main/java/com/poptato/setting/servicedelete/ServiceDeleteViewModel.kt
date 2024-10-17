package com.poptato.setting.servicedelete

import androidx.lifecycle.viewModelScope
import com.poptato.domain.usecase.mypage.UserDeleteUseCase
import com.poptato.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServiceDeleteViewModel @Inject constructor(
    private val userDeleteUseCase: UserDeleteUseCase
) : BaseViewModel<ServiceDeletePageState>(
    ServiceDeletePageState()
) {

    fun userDelete() {
        viewModelScope.launch {
            userDeleteUseCase(request = Unit).collect {
                resultResponse(it, {
                    emitEventFlow(ServiceDeleteEvent.GoBackToLogIn)
                }, { error ->
                    Timber.d("[마이페이지] 회원탈퇴 서버통신 실패 -> ${error.message}")
                })
            }
        }
    }
}