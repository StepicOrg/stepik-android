package org.stepic.droid.features.course.ui.model.course_info

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepic.droid.R

/**
 * ordering represents order of items in CourseInfoFragment
 */
enum class CourseInfoType(
        @DrawableRes val icon: Int,
        @StringRes val title: Int
) {
    ABOUT(R.drawable.ic_course_info_about, R.string.course_info_about),
    REQUIREMENTS(R.drawable.ic_course_info_requirements, R.string.course_info_requirements),
    TARGET_AUDIENCE(R.drawable.ic_course_info_target_audience, R.string.course_info_target_audience),
    INSTRUCTORS(R.drawable.ic_course_info_instructors, R.string.course_info_instructors),
    TIME_TO_COMPLETE(R.drawable.ic_course_info_time_to_complete, R.string.course_info_time_to_complete),
    LANGUAGE(R.drawable.ic_course_info_language, R.string.course_info_language),
    CERTIFICATE(R.drawable.ic_course_info_certificate, R.string.course_info_certificate),
    CERTIFICATE_DETAILS(R.drawable.ic_course_info_details, R.string.course_info_certificate_detail)
}