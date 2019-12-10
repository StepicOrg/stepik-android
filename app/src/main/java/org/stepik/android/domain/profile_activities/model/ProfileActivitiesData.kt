package org.stepik.android.domain.profile_activities.model

data class ProfileActivitiesData(
    val pins: List<Long>,
    val streak: Int,
    val maxStreak: Int,
    val isSolvedToday: Boolean
)