package org.stepic.droid.util

import org.stepic.droid.model.Course
import org.stepic.droid.model.Section


fun Section?.hasUserAccess(course: Course? = null) =
        this != null
                && (this.isActive || this.actions?.testSection != null)
                && (course?.enrollment ?: 1) > 0
                && !this.isExam
                && this.isRequirementSatisfied

fun Section?.hasUserAccessAndNotEmpty(course: Course?) =
        this.hasUserAccess(course) && this?.units?.isNotEmpty() ?: false
