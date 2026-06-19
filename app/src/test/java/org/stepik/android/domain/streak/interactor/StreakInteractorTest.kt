package org.stepik.android.domain.streak.interactor

import io.reactivex.Single
import org.junit.Test
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.UserActivity
import org.stepik.android.model.user.UserActivitySummary
import org.stepik.android.view.streak.notification.StreakNotificationScheduler

class StreakInteractorTest {
    @Test
    fun `on need show streak returns recent strike above pins limit`() {
        StreakInteractor(
            userActivityRepository = FakeUserActivityRepository(
                UserActivitySummary(
                    recentStrike = RECENT_STRIKE,
                    solvedToday = 0,
                    pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
                )
            ),
            sharedPreferenceHelper = TestSharedPreferenceHelper(Profile(id = USER_ID)),
            streakNotificationScheduler = FakeStreakNotificationScheduler()
        )
            .onNeedShowStreak()
            .test()
            .assertComplete()
            .assertResult(RECENT_STRIKE)
    }

    private class FakeUserActivityRepository(
        private val summary: UserActivitySummary
    ) : UserActivityRepository {
        override fun getUserActivities(userId: Long): Single<List<UserActivity>> =
            Single.error(UnsupportedOperationException("Legacy user activities should not be used"))

        override fun getUserActivitySummary(userId: Long): Single<UserActivitySummary> =
            Single.just(summary)
    }

    private class TestSharedPreferenceHelper(
        private val profile: Profile
    ) : SharedPreferenceHelper(null, null, null, null) {
        override fun getProfile(): Profile =
            profile
    }

    private class FakeStreakNotificationScheduler : StreakNotificationScheduler {
        override fun scheduleStreakNotification() {}
    }

    private companion object {
        const val USER_ID = 1L
        const val RECENT_STRIKE = 380
    }
}
