package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Course

interface FastContinueView {

    fun onLoading()

    fun onAnonymous()

    fun onEmptyCourse()

    fun onShowCourse(course: Course)
}
