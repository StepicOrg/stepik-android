package org.stepik.android.domain.course_complete.model

import org.stepik.android.model.Certificate
import org.stepik.android.model.Course
import org.stepik.android.model.Progress

data class CourseCompleteInfo(
    val course: Course,
    val courseProgress: Progress,
    val certificate: Certificate?,
    val hasReview: Boolean
)