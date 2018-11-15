package org.stepic.droid.model

import android.os.Parcel
import android.os.Parcelable
import org.stepik.android.model.achievements.Achievement
import org.stepik.android.model.achievements.AchievementProgress
import org.stepik.android.model.util.readBoolean
import org.stepik.android.model.util.writeBoolean

class AchievementFlatItem(
        val uploadcareUUID: String?,

        val isLocked: Boolean,
        val kind: String,
        val currentScore: Int,
        val targetScore: Int,

        val currentLevel: Int,
        val maxLevel: Int
) : Parcelable {
    constructor(parcel: Parcel): this(
            parcel.readString(),
            parcel.readBoolean(),
            parcel.readString(),
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
    ): this(
            currentLevelAchievement?.uploadcareUUID,
            currentLevelAchievement == null,
            nextLevelAchievement.kind,
            nextLevelAchievementProgress.score,
            nextLevelAchievement.targetScore,
            currentLevel,
            maxLevel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uploadcareUUID)
        parcel.writeBoolean(isLocked)
        parcel.writeString(kind)
        parcel.writeInt(currentScore)
        parcel.writeInt(targetScore)
        parcel.writeInt(currentLevel)
        parcel.writeInt(maxLevel)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<AchievementFlatItem> {
        override fun createFromParcel(parcel: Parcel) = AchievementFlatItem(parcel)
        override fun newArray(size: Int) = arrayOfNulls<AchievementFlatItem?>(size)
    }
}