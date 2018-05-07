package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Course

interface CourseJoinView {
    fun showProgress()

    fun setEnabledJoinButton(isEnabled: Boolean)

    fun onFailJoin(code: Int)

    fun onSuccessJoin(course: Course)
}
