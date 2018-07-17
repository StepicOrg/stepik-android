package org.stepic.droid.core.joining.contract

import org.stepik.android.model.structure.Course

interface JoiningPoster {
    fun joinCourse(joiningCourse: Course)
}
