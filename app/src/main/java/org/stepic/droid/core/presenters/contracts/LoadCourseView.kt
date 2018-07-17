package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.structure.Course

interface LoadCourseView {
    fun onCourseFound(course: Course)

    fun onCourseUnavailable(courseId : Long)

    fun onInternetFailWhenCourseIsTriedToLoad()
}
