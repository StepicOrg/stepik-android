package org.stepic.droid.core

import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.util.RxOptional
import org.stepik.android.model.Course
import javax.inject.Inject


@CourseGeneralScope
class FirstCourseSubjectHolder
@Inject
constructor() {
    val firstCourseSubject = BehaviorSubject.create<RxOptional<Course>>()
}
