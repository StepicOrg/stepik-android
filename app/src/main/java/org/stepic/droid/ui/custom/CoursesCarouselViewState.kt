package org.stepic.droid.ui.custom

import org.stepik.android.model.Course

data class CoursesCarouselViewState(
    val courses: List<Course>,
    val scrollPosition: Int
)