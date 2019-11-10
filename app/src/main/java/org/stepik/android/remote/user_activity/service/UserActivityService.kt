package org.stepik.android.remote.user_activity.service

import io.reactivex.Single
import org.stepik.android.remote.user_activity.model.UserActivityResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface UserActivityService {
    @GET("api/user-activities/{userId}")
    fun getUserActivities(@Path("userId") userId: Long): Call<UserActivityResponse>

    @GET("api/user-activities/{userId}")
    fun getUserActivitiesReactive(@Path("userId") userId: Long): Single<UserActivityResponse>
}