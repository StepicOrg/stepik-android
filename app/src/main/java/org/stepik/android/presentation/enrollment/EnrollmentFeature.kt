package org.stepik.android.presentation.enrollment

import org.stepik.android.model.Course

interface EnrollmentFeature {
    sealed class Message {
        data class EnrollmentMessage(val enrolledCourse: Course) : Message()
    }
    sealed class Action
}