package org.stepik.android.remote.achievement.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.achievements.Achievement
import org.stepik.android.remote.base.model.MetaResponse

class AchievementsResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("achievements")
    val achievements: List<Achievement>
) : MetaResponse