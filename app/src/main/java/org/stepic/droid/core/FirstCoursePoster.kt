package org.stepic.droid.core

import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.model.Course
import org.stepic.droid.util.RxOptional
import javax.inject.Inject

@CourseGeneralScope
class FirstCoursePoster
@Inject
constructor(
        private val firstCourseSubjectHolder: FirstCourseSubjectHolder
) {
    fun postFirstCourse(course: Course?) {
        firstCourseSubjectHolder.firstCourseSubject.onNext(RxOptional(course))
    }
}
