package org.stepic.droid.web.achievements.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.web.MetaResponseBase
import org.stepik.android.model.Meta
import org.stepik.android.model.achievements.AchievementProgress

class AchievementProgressesResponse(
        meta: Meta,

        @SerializedName("achievement-progresses")
        val achievementsProgresses: List<AchievementProgress>
): MetaResponseBase(meta)