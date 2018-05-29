package org.stepic.droid.features.deadlines.model

import java.util.Date

class DeadlineFlatItem(
        val recordId: Long,
        val courseId: Long,
        val sectionId: Long,
        val deadline: Date
)