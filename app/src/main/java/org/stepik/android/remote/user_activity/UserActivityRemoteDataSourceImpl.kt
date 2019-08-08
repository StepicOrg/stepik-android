package org.stepik.android.remote.user_activity

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.user_activity.source.UserActivityRemoteDataSource
import org.stepik.android.model.user.UserActivity
import org.stepik.android.remote.user_activity.model.UserActivityResponse
import javax.inject.Inject

class UserActivityRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : UserActivityRemoteDataSource {
    private val userActivityResponseMapper =
        Function<UserActivityResponse, List<UserActivity>>(UserActivityResponse::userActivities)

    override fun getUserActivities(userId: Long): Single<List<UserActivity>> =
        api
            .getUserActivitiesReactive(userId)
            .map(userActivityResponseMapper)
}