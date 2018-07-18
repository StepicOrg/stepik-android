package org.stepik.android.model.structure

class Enrollment(val course: Long = 0)

class EnrollmentWrapper(val enrollment: Enrollment) {
    constructor(courseId: Long): this(Enrollment(courseId))
}