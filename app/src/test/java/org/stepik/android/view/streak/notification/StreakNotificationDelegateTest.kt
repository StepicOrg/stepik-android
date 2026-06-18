package org.stepik.android.view.streak.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.UserActivitySummary
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import org.stepik.android.view.streak.model.StreakNotificationType

@RunWith(RobolectricTestRunner::class)
class StreakNotificationDelegateTest {
    @Mock
    private lateinit var analytic: Analytic

    @Mock
    private lateinit var userActivityRepository: UserActivityRepository

    @Mock
    private lateinit var screenManager: ScreenManager

    @Mock
    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper

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

        whenever(sharedPreferenceHelper.isStreakNotificationEnabled) doReturn true
        whenever(sharedPreferenceHelper.numberOfStreakNotifications) doReturn 0
        whenever(sharedPreferenceHelper.timeNotificationCode) doReturn 12
        whenever(sharedPreferenceHelper.profile) doReturn Profile(id = USER_ID)
        whenever(screenManager.getMyCoursesIntent(context)) doReturn Intent("test")
        whenever(notificationHelper.makeSimpleNotificationBuilder(any(), any(), any(), any(), any(), any())) doReturn
            NotificationCompat.Builder(context, "test")
                .setSmallIcon(R.drawable.ic_player_notification)
    }

    @Test
    fun `recent strike less than or equal to zero sends zero streak notification`() {
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(
            UserActivitySummary(recentStrike = 0, solvedToday = 0)
        )

        streakNotificationDelegate.onNeedShowNotification()

        verify(analytic).reportEvent(Analytic.Streak.GET_ZERO_STREAK_NOTIFICATION)
        verify(sharedPreferenceHelper, never()).resetNumberOfStreakNotifications()
        verifyNotificationShown(StreakNotificationType.ZERO)
    }

    @Test
    fun `positive recent strike with solved today sends improvement notification`() {
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(
            UserActivitySummary(recentStrike = 10, solvedToday = 1)
        )

        streakNotificationDelegate.onNeedShowNotification()

        verify(sharedPreferenceHelper).resetNumberOfStreakNotifications()
        verifyNotificationShown(StreakNotificationType.SOLVED_TODAY)
    }

    @Test
    fun `positive recent strike without solved today sends call to action notification`() {
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(
            UserActivitySummary(recentStrike = 10, solvedToday = 0)
        )

        streakNotificationDelegate.onNeedShowNotification()

        verify(sharedPreferenceHelper).resetNumberOfStreakNotifications()
        verifyNotificationShown(StreakNotificationType.NOT_SOLVED_TODAY)
    }

    private fun verifyNotificationShown(notificationType: StreakNotificationType) {
        val analyticEventCaptor = argumentCaptor<AnalyticEvent>()

        verify(analytic).report(analyticEventCaptor.capture())
        assertEquals(
            notificationType.type,
            analyticEventCaptor.firstValue.params["type"]
        )
        verify(stepikNotificationManager).showNotification(eq(STREAK_NOTIFICATION_ID), any())
    }

    private companion object {
        const val USER_ID = 1L
        const val STREAK_NOTIFICATION_ID = 3214L
    }
}
