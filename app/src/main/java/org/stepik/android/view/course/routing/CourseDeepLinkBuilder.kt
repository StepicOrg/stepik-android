package org.stepik.android.view.course.routing

import android.net.Uri
import org.stepic.droid.configuration.Config
import org.stepic.droid.util.appendQueryParameters
import org.stepik.android.view.base.routing.ExternalDeepLinkProcessor
import javax.inject.Inject

class CourseDeepLinkBuilder
@Inject
constructor(
    private val config: Config,
    private val externalDeepLinkProcessor: ExternalDeepLinkProcessor
) {
    fun createCourseLink(courseId: Long, tab: CourseScreenTab, queryParams: Map<String, List<String>>?): Uri =
        Uri.parse("${config.baseUrl}/$COURSE_PATH_SEGMENT/$courseId/${tab.path}")
            .buildUpon()
            .appendQueryParameters(queryParams ?: emptyMap())
            .let(externalDeepLinkProcessor::processExternalDeepLing)
            .build()
}