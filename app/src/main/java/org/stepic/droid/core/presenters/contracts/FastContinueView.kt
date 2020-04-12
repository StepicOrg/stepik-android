package org.stepic.droid.core.presenters.contracts

import org.stepik.android.domain.course_list.model.CourseListItem

interface FastContinueView {

    fun onLoading()

    fun onAnonymous()

    fun onEmptyCourse()

    fun onShowCourse(courseListItem: CourseListItem.Data)
}
