package org.stepik.android.domain.user_activity.repository

import io.reactivex.Single
import org.stepik.android.model.user.UserActivity

interface UserActivityRepository {
    fun getUserActivities(userId: Long): Single<List<UserActivity>>
}