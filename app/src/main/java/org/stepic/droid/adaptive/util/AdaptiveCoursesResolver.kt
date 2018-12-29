package org.stepic.droid.adaptive.util

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.BuildConfig
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.DbParseHelper
import javax.inject.Inject

class AdaptiveCoursesResolver
@Inject
constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
        private val userPreferences: UserPreferences
) {
    companion object {
        private const val COURSE_DELIMITER = "-"
    }

    fun isAdaptive(courseId: Long) =
            userPreferences.isAdaptiveModeEnabled &&
            firebaseRemoteConfig
                .getString(RemoteConfig.ADAPTIVE_COURSES)
                .split(",")
                .map(::parseAdaptiveCourse)
                .any {
                    courseId == it.first && BuildConfig.VERSION_CODE >= it.second
                }

    private fun parseAdaptiveCourse(course: String): Pair<Long, Long> {
        val courseId = course.substringBefore(COURSE_DELIMITER, course).toLongOrNull() ?: 0L
        val version = course.substringAfter(COURSE_DELIMITER, "0").toLongOrNull() ?: 0L
        return courseId to version
    }
}