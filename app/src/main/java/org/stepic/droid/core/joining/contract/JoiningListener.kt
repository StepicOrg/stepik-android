package org.stepic.droid.core.joining.contract

import org.stepic.droid.model.Course

interface JoiningListener {
    fun onSuccessJoin(joinedCourse: Course)
}
