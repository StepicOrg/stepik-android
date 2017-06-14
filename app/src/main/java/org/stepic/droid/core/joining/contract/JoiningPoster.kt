package org.stepic.droid.core.joining.contract

import org.stepic.droid.model.Course

interface JoiningPoster {
    fun joinCourse(joiningCourse: Course)
}
