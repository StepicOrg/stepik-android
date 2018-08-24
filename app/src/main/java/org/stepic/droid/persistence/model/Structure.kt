package org.stepic.droid.persistence.model

data class Structure(
        val course: Long,
        val section: Long,
        val unit: Long,
        val lesson: Long,
        val step: Long
)