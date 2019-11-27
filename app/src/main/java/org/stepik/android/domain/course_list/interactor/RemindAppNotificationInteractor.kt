package org.stepik.android.domain.course_list.interactor

import org.stepic.droid.model.CourseListType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.repository.CourseListRepository
import javax.inject.Inject

class RemindAppNotificationInteractor
@Inject
constructor(
    private val courseListRepository: CourseListRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    fun isNotificationShown(): Boolean {
        val isFirstDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)
        val isSevenDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)
        // already shown.
        // do not show again
        return isFirstDayNotificationShown && isSevenDayNotificationShown
    }

    fun hasUserInteractedWithApp() =
        sharedPreferenceHelper.authResponseFromStore == null ||
        sharedPreferenceHelper.isStreakNotificationEnabled ||
        hasEnrolledCourses() ||
        sharedPreferenceHelper.anyStepIsSolved()

    private fun hasEnrolledCourses(): Boolean =
        courseListRepository
            .getCourseList(CourseListType.ENROLLED, 1, getLang(), sourceType = DataSourceType.CACHE)
            .blockingGet()
            .isNotEmpty()

    private fun getLang(): String {
        val enumSet = sharedPreferenceHelper.filterForFeatured
        return enumSet.iterator().next().language
    }
}