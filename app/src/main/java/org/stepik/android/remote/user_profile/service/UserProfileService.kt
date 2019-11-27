package org.stepik.android.remote.user_profile.service

import io.reactivex.Single
import org.stepik.android.remote.auth.model.StepikProfileResponse
import retrofit2.http.GET

interface UserProfileService {
    @GET("api/stepics/1")
    fun getUserProfile(): Single<StepikProfileResponse>
}