package org.stepik.android.view.lesson.routing

import android.content.Intent
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.getPathSegmentParameter
import org.stepik.android.domain.lesson.model.LessonDeepLinkData

private const val PATH_SEGMENT_LESSON = "lesson"
private const val PATH_SEGMENT_STEP = "step"

private const val QUERY_PARAMETER_UNIT = "unit"
private const val QUERY_PARAMETER_DISCUSSION = "discussion"

fun Intent.getLessonIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(PATH_SEGMENT_LESSON)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getStepPositionFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(PATH_SEGMENT_STEP)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getUnitIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getQueryParameter(QUERY_PARAMETER_UNIT)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getDiscussionIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    val path = data
        .getQueryParameter(QUERY_PARAMETER_DISCUSSION)
        ?: return null

    return HtmlHelper.parseIdFromSlug(path)
}

fun Intent.getLessonDeepLinkData(): LessonDeepLinkData? =
    getLessonIdFromDeepLink()
        ?.let { lessonId ->
            LessonDeepLinkData(
                lessonId = lessonId,
                stepPosition = getStepPositionFromDeepLink() ?: 1,
                unitId = getUnitIdFromDeepLink(),
                discussionId = getDiscussionIdFromDeepLink()
            )
        }