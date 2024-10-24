package com.poptato.data.service

import com.poptato.data.base.ApiResponse
import com.poptato.data.base.Endpoints
import com.poptato.data.model.response.backlog.BacklogListResponse
import com.poptato.domain.model.request.backlog.CreateBacklogRequestModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BacklogService {

    @POST(Endpoints.Backlog.BACKLOG)
    suspend fun createBacklog(
        @Body request: CreateBacklogRequestModel
    ): Response<ApiResponse<Unit>>

    @GET(Endpoints.Backlog.BACKLOGS)
    suspend fun getBacklogList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<BacklogListResponse>>
}