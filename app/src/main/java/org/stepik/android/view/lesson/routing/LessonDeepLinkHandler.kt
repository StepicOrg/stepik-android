package org.stepik.android.view.lesson.routing

import android.content.Intent
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.getPathSegmentParameter

private const val LESSON_PATH_SEGMENT = "lesson"
private const val STEP_PATH_SEGMENT = "step"
private const val UNIT_QUERY_PARAMETER = "unit"

fun Intent.getLessonIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(LESSON_PATH_SEGMENT)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getStepPositionFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(STEP_PATH_SEGMENT)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getUnitIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getQueryParameter(UNIT_QUERY_PARAMETER)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}