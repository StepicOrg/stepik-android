package org.stepik.android.data.user_activity.source

import io.reactivex.Single
import org.stepik.android.model.user.UserActivity

interface UserActivityRemoteDataSource {
    fun getUserActivities(userId: Long): Single<List<UserActivity>>
}