package org.stepic.droid.features.achievements.ui.adapters

import android.net.Uri
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.R
import org.stepic.droid.features.achievements.ui.custom.AchievementCircleProgressView
import org.stepic.droid.features.achievements.ui.custom.VectorRatingBar
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.svg.GlideSvgRequestFactory

class AchievementsTileAdapter: RecyclerView.Adapter<AchievementsTileAdapter.AchievementTileViewHolder>() {
    companion object {
        private const val EMPTY_ACHIEVEMENT_ICON_PATH = "file:///android_asset/images/vector/achievements/ic_empty_achievement.svg"
    }

    private val achievements = ArrayList<AchievementFlatItem>()

    fun addAchievements(newAchievements: List<AchievementFlatItem>) {
        achievements.addAll(newAchievements)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AchievementTileViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_achievement_tile, parent, false)
    )

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(holder: AchievementTileViewHolder, position: Int) {
        val item = achievements[position]

        with(holder) {
            achievementLevels.progress = item.currentLevel
            achievementLevels.total = item.maxLevel

            achievementLevelProgress.progress = item.currentScore.toFloat() / item.targetScore

            if (item.isLocked) {
                setAchievementIcon(EMPTY_ACHIEVEMENT_ICON_PATH)
                achievementIcon.alpha = 0.4f
            } else {
                achievementIcon.alpha = 1f
                // resolve image
            }

            achievementLevelProgress.changeVisibility(!item.isLocked)
            achievementLevels.changeVisibility(!item.isLocked)
        }
    }

    class AchievementTileViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val achievementLevels: VectorRatingBar = root.achievementLevels
        val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
        val achievementIcon: ImageView = root.achievementIcon

        private val svgRequestBuilder = GlideSvgRequestFactory
                .create(itemView.context, AppCompatResources.getDrawable(itemView.context, R.drawable.general_placeholder))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)

        fun setAchievementIcon(path: String) {
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
    }
}