package com.poptato.di

import com.poptato.data.service.AuthService
import com.poptato.data.service.BacklogService
import com.poptato.data.service.HistoryService
import com.poptato.data.service.MyPageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    fun provideBacklogService(retrofit: Retrofit): BacklogService {
        return retrofit.create(BacklogService::class.java)
    }

    @Provides
    fun provideHistoryService(retrofit: Retrofit): HistoryService {
        return retrofit.create(HistoryService::class.java)
    }

    @Provides
    fun provideMyPageService(retrofit: Retrofit): MyPageService {
        return retrofit.create(MyPageService::class.java)
    }
}
