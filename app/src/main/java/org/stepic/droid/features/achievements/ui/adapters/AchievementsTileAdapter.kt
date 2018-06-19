package org.stepic.droid.features.achievements.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.ui.adapters.holders.AchievementTileViewHolder
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import javax.inject.Inject

class AchievementsTileAdapter: RecyclerView.Adapter<AchievementTileViewHolder>() {
    @Inject
    lateinit var achievementResourceResolver: AchievementResourceResolver

    init {
        App.component().inject(this)
    }

    private val achievements = ArrayList<AchievementFlatItem>()

    fun addAchievements(newAchievements: List<AchievementFlatItem>) {
        achievements.addAll(newAchievements)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AchievementTileViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_achievement_tile, parent, false),
            achievementResourceResolver
    )

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(holder: AchievementTileViewHolder, position: Int) {
        holder.onBind(achievements[position])
    }
}