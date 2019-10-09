package org.stepik.android.domain.course_payments.exception

class CourseAlreadyOwnedException(courseId: Long) : Exception("Course $courseId already owned")