package org.stepik.android.data.user_activity.repository

import io.reactivex.Single
import org.stepik.android.data.user_activity.source.UserActivityRemoteDataSource
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.UserActivity
import javax.inject.Inject

class UserActivityRepositoryImpl
@Inject
constructor(
    private val userActivityRemoteDataSource: UserActivityRemoteDataSource
) : UserActivityRepository {

    override fun getUserActivities(userId: Long): Single<List<UserActivity>> =
        userActivityRemoteDataSource.getUserActivities(userId)
}