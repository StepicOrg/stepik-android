package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class PersonalizedOnboardingCourseListsUserProperty(
    personalizedOnboardingCourseLists: String
) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.PERSONALIZED_ONBOARDING_COURSE_LISTS

    override val value: String =
        personalizedOnboardingCourseLists
}