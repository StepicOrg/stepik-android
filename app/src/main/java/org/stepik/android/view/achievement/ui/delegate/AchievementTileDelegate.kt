package org.stepik.android.view.achievement.ui.delegate

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isGone
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.R
import org.stepik.android.view.achievement.ui.resolver.AchievementResourceResolver
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import org.stepik.android.view.achievement.ui.view.AchievementCircleProgressView
import org.stepik.android.view.achievement.ui.view.VectorRatingBar

class AchievementTileDelegate(
    root: View,
    private val achievementResourceResolver: AchievementResourceResolver
) {
    private val achievementLevels: VectorRatingBar = root.achievementLevels
    private val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
    private val achievementIcon = root.achievementIcon.wrapWithGlide()

    private val achievementIconSize = root.resources.getDimensionPixelSize(R.dimen.achievement_tile_width)
    private val achievementIconPlaceholder =
        AppCompatResources.getDrawable(root.context, R.drawable.ic_achievement_empty)

    fun setAchievement(item: AchievementItem) {
        achievementLevels.progress = item.currentLevel
        achievementLevels.total = item.maxLevel

        achievementLevelProgress.progress = item.currentScore.toFloat() / item.targetScore

        achievementIcon
            .setImagePath(achievementResourceResolver.resolveAchievementIcon(item, achievementIconSize), achievementIconPlaceholder)

        val alpha = if (item.isLocked) 0.4f else 1f
        achievementIcon.imageView.alpha = alpha
        achievementLevelProgress.alpha = alpha

        achievementLevels.isGone = item.isLocked
    }
}