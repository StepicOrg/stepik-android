package org.stepic.droid.model

import java.util.*

data class UserActivity(
        val id: Long = -1,
        val pins: ArrayList<Long>
)