package org.stepik.android.remote.auth.service

import org.stepic.droid.util.AppConstants
import org.stepik.android.remote.auth.model.StepikProfileResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface EmptyAuthService {

    @GET("/")
    fun getStepicForFun(@Header("Accept-Language") lang: String): Call<Void>

    @FormUrlEncoded
    @POST("accounts/password/reset/")
    fun remindPassword(@Field(value = "email", encoded = true) email: String): Call<Void>

    @GET("api/stepics/1")
    fun getUserProfileWithCookie(
        @Header(AppConstants.refererHeaderName) referer: String,
        @Header(AppConstants.cookieHeaderName) cookies: String,
        @Header(AppConstants.csrfTokenHeaderName) csrfToken: String
    ): Call<StepikProfileResponse>
}