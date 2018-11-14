package org.stepik.android.domain.course.repository

import io.reactivex.Maybe
import org.stepik.android.model.Course

interface CourseRepository {
    fun getCourse(courseId: Long): Maybe<Course>
}