package org.stepik.android.domain.achievement.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.achievements.Achievement
import org.stepik.android.model.achievements.AchievementProgress
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean
import ru.nobird.android.core.model.Identifiable

data class AchievementItem(
    val uploadcareUUID: String?,

    val isLocked: Boolean,
    val kind: String,
    val currentScore: Int,
    val targetScore: Int,

    val currentLevel: Int,
    val maxLevel: Int
) : Parcelable, Identifiable<String> {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readBoolean(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

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

    override val id: String = kind

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uploadcareUUID)
        parcel.writeBoolean(isLocked)
        parcel.writeString(kind)
        parcel.writeInt(currentScore)
        parcel.writeInt(targetScore)
        parcel.writeInt(currentLevel)
        parcel.writeInt(maxLevel)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AchievementItem> {
        override fun createFromParcel(parcel: Parcel): AchievementItem =
            AchievementItem(parcel)

        override fun newArray(size: Int): Array<AchievementItem?> =
            arrayOfNulls<AchievementItem?>(size)
    }
}