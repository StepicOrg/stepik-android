package org.stepic.droid.core.dropping.contract

import androidx.annotation.MainThread
import org.stepik.android.model.Course

interface DroppingPoster {

    @MainThread
    fun successDropCourse(course: Course)

    @MainThread
    fun failDropCourse(course: Course)
}
