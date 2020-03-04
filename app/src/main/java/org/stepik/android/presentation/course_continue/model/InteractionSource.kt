package org.stepik.android.presentation.course_continue.model

import org.stepic.droid.analytic.AmplitudeAnalytic

enum class InteractionSource(
    val source: String
) {
    COURSE_WIDGET(AmplitudeAnalytic.Course.Values.COURSE_WIDGET),
    HOME_WIDGET(AmplitudeAnalytic.Course.Values.HOME_WIDGET),
    COURSE_SCREEN(AmplitudeAnalytic.Course.Values.COURSE_SCREEN)
}