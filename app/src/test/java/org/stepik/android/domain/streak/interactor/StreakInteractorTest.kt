package org.stepik.android.domain.streak.interactor

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.UserActivitySummary
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import org.stepik.android.view.streak.notification.StreakNotificationDelegate

@RunWith(RobolectricTestRunner::class)
class StreakInteractorTest {
    @Mock
    private lateinit var userActivityRepository: UserActivityRepository

    @Mock
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Mock
    private lateinit var analytic: Analytic

    @Mock
    private lateinit var screenManager: ScreenManager

    @Mock
    private lateinit var notificationHelper: NotificationHelper

    @Mock
    private lateinit var stepikNotificationManager: StepikNotificationManager

    private lateinit var context: Context
    private lateinit var streakNotificationDelegate: StreakNotificationDelegate

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        context = RuntimeEnvironment.getApplication()
        streakNotificationDelegate = StreakNotificationDelegate(
            context = context,
            analytic = analytic,
            userActivityRepository = userActivityRepository,
            screenManager = screenManager,
            sharedPreferenceHelper = sharedPreferenceHelper,
            notificationHelper = notificationHelper,
            stepikNotificationManager = stepikNotificationManager
        )
    }

    @Test
    fun `on need show streak returns recent strike above pins limit`() {
        whenever(sharedPreferenceHelper.profile) doReturn Profile(id = USER_ID)
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(
            UserActivitySummary(
                recentStrike = RECENT_STRIKE,
                solvedToday = 0,
                pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
            )
        )

        StreakInteractor(
            userActivityRepository = userActivityRepository,
            sharedPreferenceHelper = sharedPreferenceHelper,
            streakNotificationDelegate = streakNotificationDelegate
        )
            .onNeedShowStreak()
            .test()
            .assertComplete()
            .assertResult(RECENT_STRIKE)
    }

    private companion object {
        const val USER_ID = 1L
        const val RECENT_STRIKE = 380
    }
}
