package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class CourseRevenueAvailableUserProperty(isCourseRevenueAvailable: Boolean) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.IS_COURSE_REVENUE_AVAILABLE_ANDROID

    override val value: Boolean =
        isCourseRevenueAvailable
}