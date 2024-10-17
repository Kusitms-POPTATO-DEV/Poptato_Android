package com.poptato.data.service

import com.poptato.data.base.ApiResponse
import com.poptato.data.base.Endpoints
import retrofit2.Response
import retrofit2.http.DELETE

interface MyPageService {

    @DELETE(Endpoints.MyPage.USER_DELETE)
    suspend fun userDelete(): Response<ApiResponse<Unit>>
}