package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.CourseListItem

interface CatalogView {

    fun showCourseItems(courseItems: List<CourseListItem>)

    fun offlineMode()
}
