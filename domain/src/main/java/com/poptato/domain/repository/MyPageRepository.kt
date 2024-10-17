package com.poptato.domain.repository

import kotlinx.coroutines.flow.Flow


interface MyPageRepository {

    suspend fun userDelete(): Flow<Result<Unit>>
}