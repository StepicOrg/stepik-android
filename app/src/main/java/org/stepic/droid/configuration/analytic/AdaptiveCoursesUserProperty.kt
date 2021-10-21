package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class AdaptiveCoursesUserProperty(adaptiveCourses: String) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.ADAPTIVE_COURSES

    override val value: String =
        adaptiveCourses
}