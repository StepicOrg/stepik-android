package org.stepic.droid.web.achievements.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.Meta
import org.stepic.droid.model.achievements.AchievementProgress
import org.stepic.droid.web.MetaResponseBase

class AchievementProgressesResponse(
        meta: Meta,

        @SerializedName("achievement-progresses")
        val achievementsProgresses: List<AchievementProgress>
): MetaResponseBase(meta)