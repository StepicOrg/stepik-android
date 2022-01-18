package org.stepik.android.domain.course_list.interactor

import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import javax.inject.Inject

class RemindAppNotificationInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val userCoursesRepository: UserCoursesRepository
) {
    fun isNotificationShown(): Boolean {
        val isFirstDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)
        val isSevenDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)
        // already shown.
        // do not show again
        return isFirstDayNotificationShown && isSevenDayNotificationShown
    }

    fun hasUserInteractedWithApp(): Boolean =
        sharedPreferenceHelper.authResponseFromStore == null ||
        sharedPreferenceHelper.isStreakNotificationEnabled ||
        hasEnrolledCourses() ||
        sharedPreferenceHelper.anyStepIsSolved()

    private fun hasEnrolledCourses(): Boolean =
        userCoursesRepository
            .getUserCourses(sourceType = DataSourceType.CACHE)
            .blockingGet()
            .isNotEmpty()
}