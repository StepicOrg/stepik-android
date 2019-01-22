package org.stepik.android.domain.course_payments.exception

import java.lang.Exception

class CourseAlreadyOwnedException(courseId: Long) : Exception("Course $courseId already owned")