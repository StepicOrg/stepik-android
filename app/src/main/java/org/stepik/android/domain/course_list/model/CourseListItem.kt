package org.stepik.android.domain.course_list.model

import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

data class CourseListItem(
    val courseId: Long,
    val course: Course,
    val progress: Progress?,
    val rating: Double,
    val enrollmentState: EnrollmentState
)