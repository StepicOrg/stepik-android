package org.stepic.droid.util

import org.stepic.droid.model.Course
import org.stepic.droid.model.Section


fun Section?.hasUserAccess(course: Course? = null) =
        this != null && (this.is_active || this.actions?.test_section != null) && course?.enrollment ?: 1 > 0 && !this.isExam

