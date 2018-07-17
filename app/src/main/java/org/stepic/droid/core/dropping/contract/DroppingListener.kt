package org.stepic.droid.core.dropping.contract

import org.stepik.android.model.structure.Course

interface DroppingListener {

    fun onSuccessDropCourse(course: Course)

    fun onFailDropCourse(course: Course)
}
