package org.stepic.droid.web.achievements.model

import org.stepik.android.model.achievements.Achievement
import org.stepic.droid.web.MetaResponseBase
import org.stepik.android.model.Meta

class AchievementsResponse(
        meta: Meta,
        val achievements: List<Achievement>
): MetaResponseBase(meta)