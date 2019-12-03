package org.stepik.android.remote.achievement.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.achievements.AchievementProgress
import org.stepik.android.remote.base.model.MetaResponse

class AchievementProgressesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("achievement-progresses")
    val achievementsProgresses: List<AchievementProgress>
) : MetaResponse