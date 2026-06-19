package org.stepic.droid.core.presenters

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.UserActivitySummary

@RunWith(MockitoJUnitRunner::class)
class HomeStreakPresenterTest {
    @Mock
    private lateinit var userActivityRepository: UserActivityRepository

    @Mock
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Mock
    private lateinit var homeStreakView: HomeStreakView

    @Test
    fun `home streak displays recent strike above pins limit`() {
        whenever(sharedPreferenceHelper.profile) doReturn Profile(id = USER_ID)
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(
            UserActivitySummary(
                recentStrike = RECENT_STRIKE,
                solvedToday = 0,
                pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
            )
        )

        val presenter = HomeStreakPresenter(
            backgroundScheduler = Schedulers.trampoline(),
            mainScheduler = Schedulers.trampoline(),
            userActivityRepository = userActivityRepository,
            sharedPreferences = sharedPreferenceHelper
        )

        presenter.attachView(homeStreakView)
        presenter.onNeedShowStreak()

        verify(homeStreakView).showStreak(RECENT_STRIKE)
        verify(homeStreakView, never()).onEmptyStreak()
    }

    private companion object {
        const val USER_ID = 1L
        const val RECENT_STRIKE = 380
    }
}
