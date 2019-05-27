package org.stepik.android.remote.profile.service

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.remote.profile.model.ProfilePasswordRequest
import org.stepik.android.remote.profile.model.ProfileRequest
import org.stepik.android.remote.profile.model.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileService {
    @GET("api/stepics/1")
    fun getProfile(): Single<ProfileResponse>

    @PUT("api/profiles/{profileId}")
    fun saveProfile(
        @Path("profileId") profileId: Long,
        @Body profileRequest: ProfileRequest
    ): Single<ProfileResponse>

    @POST("api/profiles/{profileId}/change-password")
    fun saveProfilePassword(
        @Path("profileId") profileId: Long,
        @Body profilePasswordRequest: ProfilePasswordRequest
    ): Completable
}