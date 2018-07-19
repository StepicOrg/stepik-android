package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.Course

interface CourseJoinView {
    fun showProgress()

    fun setEnabledJoinButton(isEnabled: Boolean)

    fun onFailJoin(code: Int)

    fun onSuccessJoin(course: Course)
}
