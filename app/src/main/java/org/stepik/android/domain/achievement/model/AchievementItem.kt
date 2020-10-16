package org.stepik.android.domain.achievement.model

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.stepik.android.model.achievements.Achievement
import org.stepik.android.model.achievements.AchievementProgress
import ru.nobird.android.core.model.Identifiable

@Parcelize
data class AchievementItem(
    val uploadcareUUID: String?,

    val isLocked: Boolean,
    val kind: String,
    val currentScore: Int,
    val targetScore: Int,

    val currentLevel: Int,
    val maxLevel: Int
) : Parcelable, Identifiable<String> {
    @IgnoredOnParcel
    override val id: String = kind

    constructor(
        currentLevelAchievement: Achievement?,
        nextLevelAchievement: Achievement,
        nextLevelAchievementProgress: AchievementProgress,

        currentLevel: Int,
        maxLevel: Int
    ) : this(
        currentLevelAchievement?.uploadcareUUID,
        currentLevelAchievement == null,
        nextLevelAchievement.kind,
        nextLevelAchievementProgress.score,
        nextLevelAchievement.targetScore,
        currentLevel,
        maxLevel
    )
}