package com.poptato.di

import com.poptato.data.datastore.PoptatoDataStore
import com.poptato.domain.model.request.ReissueRequestModel
import com.poptato.domain.model.response.auth.TokenModel
import com.poptato.domain.repository.AuthRepository
import com.poptato.domain.usecase.auth.GetTokenUseCase
import com.poptato.domain.usecase.auth.ReissueTokenUseCase
import com.poptato.domain.usecase.auth.SaveTokenUseCase
import dagger.Lazy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val getTokenUseCase: GetTokenUseCase,
    private val saveTokenUseCase: SaveTokenUseCase,
    private val reissueTokenUseCase: ReissueTokenUseCase
): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val tokens = runBlocking {
            getTokenUseCase.invoke(Unit).firstOrNull()
        }

        return tokens?.let { tokenModel ->
            runBlocking {
                try {
                    val newTokensResult = reissueTokenUseCase.invoke(
                        ReissueRequestModel(
                            accessToken = tokenModel.accessToken,
                            refreshToken = tokenModel.refreshToken
                        )
                    ).firstOrNull()

                    newTokensResult?.getOrNull()?.let { newTokens ->
                        saveTokenUseCase.invoke(newTokens).collect {}

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } ?: run {
                        Timber.e("Token reissue failed: 새로운 토큰 없음")
                        null
                    }
                } catch (e: Exception) {
                    Timber.e("Token reissue failed: ${e.message}")
                    null
                }
            }
        } ?: run {
            Timber.e("유효한 토큰 없음")
            null
        }
    }
}