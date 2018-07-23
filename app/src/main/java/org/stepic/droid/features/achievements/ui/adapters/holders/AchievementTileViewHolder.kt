package org.stepic.droid.features.achievements.ui.adapters.holders

import android.view.View
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.features.achievements.ui.custom.AchievementCircleProgressView
import org.stepic.droid.features.achievements.ui.custom.VectorRatingBar
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.ui.adapters.viewhoders.GenericViewHolder
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.wrapWithGlide

class AchievementTileViewHolder(
        root: View,
        private val achievementResourceResolver: AchievementResourceResolver
): GenericViewHolder<AchievementFlatItem>(root) {
    private val achievementLevels: VectorRatingBar = root.achievementLevels
    private val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
    private val achievementIcon = root.achievementIcon.wrapWithGlide()

    override fun onBind(item: AchievementFlatItem) {
        achievementLevels.progress = item.currentLevel
        achievementLevels.total = item.maxLevel

        achievementLevelProgress.progress = item.currentScore.toFloat() / item.targetScore

        achievementIcon.setImagePath(achievementResourceResolver.resolveAchievementIcon(item, achievementIcon.imageView))

        val alpha = if (item.isLocked) 0.4f else 1f
        achievementIcon.imageView.alpha = alpha
        achievementLevelProgress.alpha = alpha

        achievementLevels.changeVisibility(!item.isLocked)
    }
}