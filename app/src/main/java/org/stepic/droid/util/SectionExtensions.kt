package org.stepic.droid.util

import org.stepik.android.model.Course
import org.stepik.android.model.Section


fun Section?.hasUserAccess(course: Course? = null) =
    this != null &&
            (this.isActive && this.isRequirementSatisfied || this.actions?.testSection != null) &&
            (course?.enrollment ?: 1) > 0 &&
            !this.isExam

fun Section?.hasUserAccessAndNotEmpty(course: Course?) =
    this.hasUserAccess(course) && this?.units?.isNotEmpty() ?: false
