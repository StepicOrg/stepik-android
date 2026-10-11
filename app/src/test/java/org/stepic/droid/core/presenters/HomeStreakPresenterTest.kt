package org.stepic.droid.core.presenters

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.UserActivity
import org.stepik.android.model.user.UserActivitySummary

class HomeStreakPresenterTest {
    @Test
    fun `home streak displays recent strike above pins limit`() {
        val view = FakeHomeStreakView()
        val presenter = HomeStreakPresenter(
            backgroundScheduler = Schedulers.trampoline(),
            mainScheduler = Schedulers.trampoline(),
            userActivityRepository = FakeUserActivityRepository(
                UserActivitySummary(
                    recentStrike = RECENT_STRIKE,
                    solvedToday = 0,
                    pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
                )
            ),
            sharedPreferences = TestSharedPreferenceHelper(Profile(id = USER_ID))
        )

        presenter.attachView(view)
        presenter.onNeedShowStreak()

        assertEquals(RECENT_STRIKE, view.shownStreak)
        assertFalse(view.isEmptyStreakShown)
    }

    private class FakeHomeStreakView : HomeStreakView {
        var shownStreak: Int? = null
            private set

        var isEmptyStreakShown: Boolean = false
            private set

        override fun showStreak(streak: Int) {
            shownStreak = streak
        }

        override fun onEmptyStreak() {
            isEmptyStreakShown = true
        }
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

    private companion object {
        const val USER_ID = 1L
        const val RECENT_STRIKE = 380
    }
}
