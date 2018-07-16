package org.stepik.android.model.learning

class Enrollment(val course: Long = 0)

class EnrollmentWrapper(val enrollment: Enrollment) {
    constructor(courseId: Long): this(Enrollment(courseId))
}