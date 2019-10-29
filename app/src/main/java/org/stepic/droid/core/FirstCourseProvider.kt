package org.stepic.droid.core

import io.reactivex.Observable
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.util.RxOptional
import org.stepik.android.model.Course
import javax.inject.Inject

@CourseGeneralScope
class FirstCourseProvider
@Inject
constructor(private val firstCourseSubjectHolder: FirstCourseSubjectHolder) {
    fun firstCourse(): Observable<RxOptional<Course>> = firstCourseSubjectHolder.firstCourseSubject
}
