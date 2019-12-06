package org.stepik.android.remote.social_profile.service

import io.reactivex.Single
import org.stepik.android.remote.social_profile.model.SocialProfilesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SocialProfilesService {
    @GET("api/social-profiles")
    fun getSocialProfiles(@Query("ids[]") ids: LongArray): Single<SocialProfilesResponse>
}