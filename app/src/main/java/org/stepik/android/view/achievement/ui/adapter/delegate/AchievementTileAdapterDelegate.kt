package org.stepik.android.view.achievement.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.achievement.ui.resolver.AchievementResourceResolver
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.view.achievement.ui.delegate.AchievementTileDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AchievementTileAdapterDelegate(
    private val achievementResourceResolver: AchievementResourceResolver,
    private val onItemClicked: (AchievementItem) -> Unit
) : AdapterDelegate<AchievementItem, DelegateViewHolder<AchievementItem>>() {
    override fun isForViewType(position: Int, data: AchievementItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AchievementItem> =
        ViewHolder(createView(parent, R.layout.view_achievement_tile))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AchievementItem>(root) {

        private val achievementTileDelegate = AchievementTileDelegate(root, achievementResourceResolver)

        init {
            root.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: AchievementItem) {
            achievementTileDelegate.setAchievement(data)
        }
    }
}