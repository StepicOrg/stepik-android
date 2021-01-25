package org.stepik.android.view.story_deeplink.routing

import android.content.Intent
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.getPathSegmentParameter

internal const val STORY_TEMPLATE_PATH_SEGMENT = "story-template"

fun Intent.getStoryId(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(STORY_TEMPLATE_PATH_SEGMENT)
        ?: data.lastPathSegment

    return HtmlHelper.parseIdFromSlug(path)
}