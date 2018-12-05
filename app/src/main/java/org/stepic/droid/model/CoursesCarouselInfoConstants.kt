package org.stepic.droid.model

import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.storage.structure.DbStructureCourseList

object CoursesCarouselInfoConstants {
    val myCourses = CoursesCarouselInfo(
            CoursesCarouselColorType.Light,
            App.getAppContext().getString(R.string.my_courses_title),
            DbStructureCourseList.Type.ENROLLED,
            null)

    val popular = CoursesCarouselInfo(
            CoursesCarouselColorType.Dark,
            App.getAppContext().getString(R.string.popular_courses_title),
            DbStructureCourseList.Type.FEATURED,
            null)
}
