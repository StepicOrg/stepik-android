package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Course

interface FastContinueView {

    fun onLoading()

    fun onAnonymous()

    fun onEmptyCourse()

    fun onShowCourse(course: Course)
}
