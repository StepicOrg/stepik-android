package org.stepic.droid.features.achievements.ui.adapters.holders

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.features.achievements.ui.custom.AchievementCircleProgressView
import org.stepic.droid.features.achievements.ui.custom.VectorRatingBar
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.adapters.viewhoders.GenericViewHolder
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.svg.GlideSvgRequestFactory

class AchievementTileViewHolder(
        root: View,
        private val achievementResourceResolver: AchievementResourceResolver
): GenericViewHolder<AchievementFlatItem>(root) {
    private val achievementLevels: VectorRatingBar = root.achievementLevels
    private val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
    private val achievementIcon: ImageView = root.achievementIcon

    private val svgRequestBuilder = GlideSvgRequestFactory
            .create(itemView.context, null)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)

    private fun setAchievementIcon(path: String) {
        if (path.endsWith(AppConstants.SVG_EXTENSION)) {
            svgRequestBuilder
                    .load(Uri.parse(path))
                    .into(achievementIcon)
        } else {
            Glide.with(itemView.context)
                    .load(path)
                    .asBitmap()
                    .into(achievementIcon)
        }
    }

    override fun onBind(item: AchievementFlatItem) {
        achievementLevels.progress = item.currentLevel
        achievementLevels.total = item.maxLevel

        achievementLevelProgress.progress = item.currentScore.toFloat() / item.targetScore

        setAchievementIcon(achievementResourceResolver.resolveAchievementIcon(item, achievementIcon))

        val alpha = if (item.isLocked) 0.4f else 1f
        achievementIcon.alpha = alpha
        achievementLevelProgress.alpha = alpha

        achievementLevels.changeVisibility(!item.isLocked)
    }
}