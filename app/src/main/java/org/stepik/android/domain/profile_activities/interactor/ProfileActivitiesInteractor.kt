package org.stepik.android.domain.profile_activities.interactor

import io.reactivex.Single
import org.stepik.android.domain.profile_activities.model.ProfileActivitiesData
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.UserActivitySummary
import javax.inject.Inject

class ProfileActivitiesInteractor
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository
) {
    fun getProfileActivities(userId: Long): Single<ProfileActivitiesData> =
        userActivityRepository
            .getUserActivitySummary(userId)
            .map(::mapToProfileActivities)

    private fun mapToProfileActivities(userActivitySummary: UserActivitySummary): ProfileActivitiesData =
        ProfileActivitiesData(
            pins = userActivitySummary.pins,
            streak = userActivitySummary.recentStrike,
            maxStreak = userActivitySummary.maxStrike,
            isSolvedToday = userActivitySummary.solvedToday > 0
        )
}