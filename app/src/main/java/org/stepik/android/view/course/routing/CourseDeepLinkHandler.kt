package org.stepik.android.view.course.routing

import android.content.Intent
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.HtmlHelper

fun Intent.getCourseIdFromDeepLink(): Long? {
    val data = this.data ?: return null

    var path = data.lastPathSegment
    if (path == AppConstants.APP_INDEXING_COURSE_DETAIL_MANIFEST_HACK) {
        data.pathSegments?.takeIf { it.size >= 2 }?.let {
            path = it[it.size - 2]
        }
    }

    return HtmlHelper.parseIdFromSlug(path)
}