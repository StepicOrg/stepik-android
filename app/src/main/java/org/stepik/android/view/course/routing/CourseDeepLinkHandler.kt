package org.stepik.android.view.course.routing

import android.content.Intent
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.HtmlHelper

private const val COURSE_PATH_SEGMENT = "course"

fun Intent.getCourseIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .pathSegments
        .indexOf(COURSE_PATH_SEGMENT)
        .takeIf { it >= 0 }
        ?.let { data.pathSegments.getOrNull(it + 1) }
        ?: data.lastPathSegment

    return HtmlHelper.parseIdFromSlug(path)
}


enum class CourseScreenTab(val path: String) {
    INFO(""),
    REVIEWS("reviews"),
    SYLLABUS("syllabus"),
    COMMENTS("comments"),
    NEWS("news"),
    PAY("pay")
}

fun Intent.getCourseTabFromDeepLink(): CourseScreenTab {
    val data = this.data ?: return CourseScreenTab.INFO

    return data
        .pathSegments
        .indexOf(COURSE_PATH_SEGMENT)
        .takeIf { it >= 0 }
        ?.let { data.pathSegments.getOrNull(it + 2) }
        ?.let { path -> CourseScreenTab.values().find { it.path == path } }
        ?: CourseScreenTab.INFO
}