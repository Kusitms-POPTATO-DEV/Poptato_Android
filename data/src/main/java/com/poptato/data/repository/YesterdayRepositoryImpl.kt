package com.poptato.data.repository

import com.poptato.data.base.BaseRepository
import com.poptato.data.mapper.YesterdayListResponseMapper
import com.poptato.data.service.YesterdayService
import com.poptato.domain.model.response.yesterday.YesterdayListModel
import com.poptato.domain.repository.YesterdayRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class YesterdayRepositoryImpl @Inject constructor(
    private val yesterdayService: YesterdayService
) : YesterdayRepository, BaseRepository() {

    override suspend fun getYesterdayList(page: Int, size: Int): Flow<Result<YesterdayListModel>> {
        return apiLaunch(
            apiCall = { yesterdayService.getYesterdayList(page, size) },
            YesterdayListResponseMapper
        )
    }
}