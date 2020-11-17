package org.stepik.android.view.course_list.routing

import android.content.Intent
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.getPathSegmentParameter

internal const val COURSE_COLLECTION_PATH_SEGMENT = "catalog"

fun Intent.getCourseListCollectionId(): Long? {
    val data = this.data ?: return null

    val path = data
        .getPathSegmentParameter(COURSE_COLLECTION_PATH_SEGMENT)
        ?: data.lastPathSegment

    return HtmlHelper.parseIdFromSlug(path)
}