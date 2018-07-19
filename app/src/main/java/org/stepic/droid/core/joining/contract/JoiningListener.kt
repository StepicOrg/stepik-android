package org.stepic.droid.core.joining.contract

import org.stepik.android.model.Course

interface JoiningListener {
    fun onSuccessJoin(joinedCourse: Course)
}
