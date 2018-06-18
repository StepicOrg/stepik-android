package org.stepic.droid.web.achievements.model

import org.stepic.droid.model.Meta
import org.stepic.droid.model.achievements.Achievement
import org.stepic.droid.web.MetaResponseBase

class AchievementsResponse(
        meta: Meta,
        val achievements: List<Achievement>
): MetaResponseBase(meta)