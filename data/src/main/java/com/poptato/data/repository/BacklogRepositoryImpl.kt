package com.poptato.data.repository

import com.poptato.data.base.BaseRepository
import com.poptato.data.mapper.BacklogListResponseMapper
import com.poptato.data.mapper.UnitResponseMapper
import com.poptato.data.service.BacklogService
import com.poptato.domain.model.request.backlog.CreateBacklogRequestModel
import com.poptato.domain.model.response.backlog.BacklogListModel
import com.poptato.domain.repository.BacklogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BacklogRepositoryImpl @Inject constructor(
    private val backlogService: BacklogService
): BacklogRepository, BaseRepository() {
    override suspend fun createBacklog(request :CreateBacklogRequestModel): Flow<Result<Unit>> {
        return apiLaunch(apiCall = { backlogService.createBacklog(request) }, UnitResponseMapper )
    }

    override suspend fun getBacklogList(page: Int, size: Int): Flow<Result<BacklogListModel>> {
        return apiLaunch(apiCall = { backlogService.getBacklogList(page, size) }, BacklogListResponseMapper)
    }
}