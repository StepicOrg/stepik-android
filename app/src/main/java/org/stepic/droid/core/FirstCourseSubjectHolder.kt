package org.stepic.droid.core

import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepik.android.model.structure.Course
import org.stepic.droid.util.RxOptional
import javax.inject.Inject


@CourseGeneralScope
class FirstCourseSubjectHolder
@Inject
constructor() {
    val firstCourseSubject = BehaviorSubject.create<RxOptional<Course>>()
}
