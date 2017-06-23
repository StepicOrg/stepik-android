package org.stepic.droid.core.dropping.contract

import org.stepic.droid.model.Course

interface DroppingPoster {
    fun successDropCourse(course: Course)

    fun failDropCourse(course: Course)
}
