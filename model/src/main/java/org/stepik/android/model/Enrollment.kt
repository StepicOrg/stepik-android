package org.stepik.android.model

class Enrollment(val course: Long = 0)

class EnrollmentWrapper(val enrollment: Enrollment) {
    constructor(courseId: Long): this(Enrollment(courseId))
}