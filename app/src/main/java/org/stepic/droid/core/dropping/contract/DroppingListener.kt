package org.stepic.droid.core.dropping.contract

import org.stepic.droid.model.Course

interface DroppingListener {

    fun onSuccessDropCourse(course: Course)

    fun onFailDropCourse(course: Course)
}
