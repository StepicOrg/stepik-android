package org.stepik.android.domain.profile_activities.interactor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepik.android.domain.profile_activities.model.ProfileActivitiesData
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.UserActivitySummary

@RunWith(MockitoJUnitRunner::class)
class ProfileActivitiesInteractorTest {
    @Mock
    private lateinit var userActivityRepository: UserActivityRepository

    @Test
    fun `summary without solved today maps to continue streak data`() {
        val pins = listOf(0L, 2L, 1L, 3L, 0L, 1L, 4L)
        val summary = UserActivitySummary(
            recentStrike = 377,
            solvedToday = 0,
            maxStrike = 412,
            pins = pins
        )
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(summary)

        ProfileActivitiesInteractor(userActivityRepository)
            .getProfileActivities(USER_ID)
            .test()
            .assertComplete()
            .assertResult(
                ProfileActivitiesData(pins, streak = 377, maxStreak = 412, isSolvedToday = false)
            )

        verify(userActivityRepository).getUserActivitySummary(USER_ID)
    }

    @Test
    fun `summary with solved today maps to active streak data`() {
        val pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
        val summary = UserActivitySummary(
            recentStrike = 378,
            solvedToday = 2,
            maxStrike = 412,
            pins = pins
        )
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(summary)

        ProfileActivitiesInteractor(userActivityRepository)
            .getProfileActivities(USER_ID)
            .test()
            .assertComplete()
            .assertResult(
                ProfileActivitiesData(pins, streak = 378, maxStreak = 412, isSolvedToday = true)
            )

        verify(userActivityRepository).getUserActivitySummary(USER_ID)
    }

    @Test
    fun `zero summary maps to start streak data`() {
        val pins = listOf(0L, 0L, 0L, 0L, 0L, 0L, 0L)
        val summary = UserActivitySummary(
            recentStrike = 0,
            solvedToday = 0,
            maxStrike = 0,
            pins = pins
        )
        whenever(userActivityRepository.getUserActivitySummary(USER_ID)) doReturn Single.just(summary)

        ProfileActivitiesInteractor(userActivityRepository)
            .getProfileActivities(USER_ID)
            .test()
            .assertComplete()
            .assertResult(
                ProfileActivitiesData(pins, streak = 0, maxStreak = 0, isSolvedToday = false)
            )

        verify(userActivityRepository).getUserActivitySummary(USER_ID)
    }

    private companion object {
        const val USER_ID = 1L
    }
}
