package com.poptato.data.service

import com.poptato.data.base.ApiResponse
import com.poptato.data.base.Endpoints
import com.poptato.data.model.response.history.HistoryListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HistoryService {

    @GET(Endpoints.History.HISTORIES)
    suspend fun getHistoryList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<HistoryListResponse>>
}