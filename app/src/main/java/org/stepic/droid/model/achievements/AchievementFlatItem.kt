package org.stepic.droid.model.achievements

import android.os.Parcel
import android.os.Parcelable

class AchievementFlatItem(
        val iconId: Long?,

        val isLocked: Boolean,
        val kind: String,
        val currentScore: Int,
        val targetScore: Int,

        val currentLevel: Int,
        val maxLevel: Int
) : Parcelable {
    constructor(parcel: Parcel): this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readByte() != 0.toByte(),
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
            null,
            currentLevelAchievement == null,
            nextLevelAchievement.kind,
            nextLevelAchievementProgress.score,
            nextLevelAchievement.targetScore,
            currentLevel,
            maxLevel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(iconId)
        parcel.writeByte(if (isLocked) 1 else 0)
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