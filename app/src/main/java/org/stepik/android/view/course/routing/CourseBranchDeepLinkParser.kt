package org.stepik.android.view.course.routing

import org.json.JSONObject
import org.stepic.droid.analytic.BranchParams
import org.stepik.android.view.routing.deeplink.BranchDeepLinkParser
import org.stepik.android.view.routing.deeplink.BranchRoute
import javax.inject.Inject

class CourseBranchDeepLinkParser
@Inject
constructor() : BranchDeepLinkParser {
    companion object {
        private const val SCREEN_COURSE = "course"
        private const val FIELD_COURSE = "course"
    }

    override fun parseBranchDeepLink(params: JSONObject): BranchRoute? =
        params
            .optLong(FIELD_COURSE, -1L)
            .takeIf { it != -1L }
            ?.takeIf { params.optString(BranchParams.FIELD_SCREEN) == SCREEN_COURSE }
            ?.let(::CourseScreenBranchRoute)
}