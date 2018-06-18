package org.stepic.droid.features.achievements.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.view_achievement_tile.view.*
import org.stepic.droid.R
import org.stepic.droid.features.achievements.ui.custom.AchievementCircleProgressView
import org.stepic.droid.features.achievements.ui.custom.VectorRatingBar
import org.stepic.droid.model.achievements.AchievementFlatItem

class AchievementsTileAdapter: RecyclerView.Adapter<AchievementsTileAdapter.AchievementTileViewHolder>() {
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
        }
    }

    class AchievementTileViewHolder(root: View): RecyclerView.ViewHolder(root) {
        val achievementLevels: VectorRatingBar = root.achievementLevels
        val achievementLevelProgress: AchievementCircleProgressView = root.achievementLevelProgress
        val icon: ImageView = root.achievementIcon
    }
}