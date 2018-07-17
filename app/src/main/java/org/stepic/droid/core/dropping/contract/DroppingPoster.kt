package org.stepic.droid.core.dropping.contract

import android.support.annotation.MainThread
import org.stepik.android.model.structure.Course

interface DroppingPoster {

    @MainThread
    fun successDropCourse(course: Course)

    @MainThread
    fun failDropCourse(course: Course)
}
