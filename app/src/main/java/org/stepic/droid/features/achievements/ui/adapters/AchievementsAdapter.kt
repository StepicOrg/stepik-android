package org.stepic.droid.features.achievements.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.ui.adapters.holders.AchievementViewHolder

class AchievementsAdapter: BaseAchievementsAdapter<AchievementViewHolder>() {
    init {
        App.component().inject(this)
    }

    override fun createItemViewHolder(parent: ViewGroup, viewType: Int) = AchievementViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_achievement_item, parent, false),
            achievementResourceResolver
    )
}