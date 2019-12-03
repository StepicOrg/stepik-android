package org.stepik.android.view.achievement.ui.delegate

import android.view.View
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.ui.util.wrapWithGlide
import org.stepik.android.view.achievement.ui.view.AchievementCircleProgressView
import org.stepik.android.view.achievement.ui.view.VectorRatingBar

class AchievementTileDelegate(
    root: View,
    private val achievementResourceResolver: AchievementResourceResolver
) {
    private val achievementLevels: VectorRatingBar = root.achievementLevels
    private val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
    private val achievementIcon = root.achievementIcon.wrapWithGlide()

    fun setAchievement(item: AchievementFlatItem) {
        achievementLevels.progress = item.currentLevel
        achievementLevels.total = item.maxLevel

        achievementLevelProgress.progress = item.currentScore.toFloat() / item.targetScore

        achievementIcon.setImagePath(achievementResourceResolver.resolveAchievementIcon(item, achievementIcon.imageView))

        val alpha = if (item.isLocked) 0.4f else 1f
        achievementIcon.imageView.alpha = alpha
        achievementLevelProgress.alpha = alpha

        achievementLevels.isGone = item.isLocked
    }
}