package org.stepik.android.cache.personal_deadlines.model

import java.util.Date

class DeadlineEntity(
    val recordId: Long,
    val courseId: Long,
    val sectionId: Long,
    val deadline: Date
)