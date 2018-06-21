package org.stepic.droid.features.achievements.ui.adapters

import android.support.v7.widget.RecyclerView
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.achievements.AchievementFlatItem
import org.stepic.droid.ui.adapters.viewhoders.GenericViewHolder
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class BaseAchievementsAdapter<VH: GenericViewHolder<AchievementFlatItem>>: RecyclerView.Adapter<VH>() {
    protected lateinit var achievementResourceResolver: AchievementResourceResolver
        @Inject set

    var achievements: List<AchievementFlatItem> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(achievements[position])
    }
}