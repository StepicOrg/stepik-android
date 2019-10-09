package org.stepic.droid.features.achievements.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.ui.adapters.viewhoders.GenericViewHolder
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class BaseAchievementsAdapter<VH: GenericViewHolder<AchievementFlatItem>>: RecyclerView.Adapter<VH>() {
    protected lateinit var achievementResourceResolver: AchievementResourceResolver
        @Inject set

    var achievements: List<AchievementFlatItem> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    var onAchievementItemClick: ((AchievementFlatItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = createItemViewHolder(parent, viewType)
        holder.itemView.setOnClickListener {
            onAchievementItemClick?.invoke(achievements[holder.adapterPosition])
        }
        return holder
    }

    protected abstract fun createItemViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun getItemCount(): Int = achievements.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(achievements[position])
    }
}