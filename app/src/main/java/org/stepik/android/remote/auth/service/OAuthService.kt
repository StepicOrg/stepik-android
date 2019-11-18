package org.stepik.android.remote.auth.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.web.UserRegistrationRequest
import org.stepik.android.remote.auth.model.OAuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OAuthService {
    @FormUrlEncoded
    @POST("/oauth2/token/")
    fun updateToken(
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Call<OAuthResponse>

    @FormUrlEncoded
    @POST("/oauth2/token/")
    fun authWithLoginPassword(
        @Field("grant_type") grant_type: String,
        @Field(value = "username", encoded = true) username: String,
        @Field(value = "password", encoded = true) password: String
    ): Single<OAuthResponse>

    @FormUrlEncoded
    @POST("/oauth2/token/")
    fun getTokenByCode(
        @Field("grant_type") grant_type: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Single<OAuthResponse>

    @FormUrlEncoded
    @POST("/oauth2/social-token/")
    fun getTokenByNativeCode(
        @Field("provider") providerName: String,
        @Field("code") providerCode: String,
        @Field("grant_type") grantType: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_type") accessToken: String?,
        @Field("email") email: String?
    ): Single<OAuthResponse>

    @POST("/api/users")
    fun createAccount(@Body user: UserRegistrationRequest): Completable
}