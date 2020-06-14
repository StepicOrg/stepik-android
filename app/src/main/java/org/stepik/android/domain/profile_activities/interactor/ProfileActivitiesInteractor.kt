package org.stepik.android.domain.profile_activities.interactor

import io.reactivex.Single
import org.stepic.droid.util.first
import org.stepik.android.domain.profile_activities.model.ProfileActivitiesData
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.UserActivity
import javax.inject.Inject
import kotlin.math.max

class ProfileActivitiesInteractor
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository
) {
    fun getProfileActivities(userId: Long): Single<ProfileActivitiesData> =
        userActivityRepository
            .getUserActivities(userId)
            .first()
            .map(::mapToProfileActivities)

    private fun mapToProfileActivities(userActivity: UserActivity): ProfileActivitiesData {
        var streak = 0
        var maxStreak = 0
        var buffer = 0

        for (i in 0..userActivity.pins.size) {
            val pin = userActivity.pins.getOrElse(i) { 0 }
            if (pin > 0) {
                buffer++
            } else {
                maxStreak = max(maxStreak, buffer)
                if (buffer == i || buffer == i - 1) { // first is solved today, second not solved
                    streak = buffer
                }
                buffer = 0
            }
        }

        return ProfileActivitiesData(userActivity.pins, streak, maxStreak, isSolvedToday = userActivity.pins[0] > 0)
    }
}