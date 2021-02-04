package org.stepik.android.view.course.routing

import android.content.Intent
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.getPathSegmentParameter

internal const val COURSE_PATH_SEGMENT = "course"

internal const val QUERY_PARAMETER_PROMO = "promo"

fun Intent.getCourseIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(COURSE_PATH_SEGMENT)
        ?: data.lastPathSegment

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getPromoCodeFromDeepLink(): String? =
    this.data?.getQueryParameter(QUERY_PARAMETER_PROMO)

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