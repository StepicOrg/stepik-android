package org.stepik.android.data.user_activity.source

import io.reactivex.Single
import org.stepik.android.model.user.UserActivity
import org.stepik.android.remote.user_activity.model.UserActivityResponse
import retrofit2.Call

interface UserActivityRemoteDataSource {
    fun getUserActivities(userId: Long): Call<UserActivityResponse>
    fun getUserActivitiesRx(userId: Long): Single<List<UserActivity>>
}