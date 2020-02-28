package org.stepik.android.view.achievement.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.view_achievement_item.view.*
import org.stepic.droid.R
import org.stepik.android.view.achievement.ui.resolver.AchievementResourceResolver
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.view.achievement.ui.delegate.AchievementTileDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AchievementAdapterDelegate(
    private val achievementResourceResolver: AchievementResourceResolver,
    private val onItemClicked: (AchievementItem) -> Unit
) : AdapterDelegate<AchievementItem, DelegateViewHolder<AchievementItem>>() {
    override fun isForViewType(position: Int, data: AchievementItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AchievementItem> =
        ViewHolder(createView(parent, R.layout.view_achievement_item))

    private inner class ViewHolder(override val containerView: View) : DelegateViewHolder<AchievementItem>(containerView), LayoutContainer {
        private val achievementTitle: TextView = containerView.achievementTitle
        private val achievementDescription: TextView = containerView.achievementDescription

        private val achievementTileDelegate = AchievementTileDelegate(containerView.achievementTile, achievementResourceResolver)

        init {
            containerView.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: AchievementItem) {
            achievementTileDelegate.setAchievement(data)

            achievementTitle.text = achievementResourceResolver.resolveTitleForKind(data.kind)
            achievementDescription.text = achievementResourceResolver.resolveDescription(data)
        }
    }
}