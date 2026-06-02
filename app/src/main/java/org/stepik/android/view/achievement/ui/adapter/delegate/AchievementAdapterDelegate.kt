package org.stepik.android.view.achievement.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewAchievementItemBinding
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

    private inner class ViewHolder(root: View) : DelegateViewHolder<AchievementItem>(root) {
        private val viewBinding: ViewAchievementItemBinding by viewBinding { ViewAchievementItemBinding.bind(root) }

        private val achievementTileDelegate = AchievementTileDelegate(viewBinding.achievementTile.root, achievementResourceResolver)

        init {
            root.setOnClickListener { itemData?.let(onItemClicked) }
        }

        override fun onBind(data: AchievementItem) {
            achievementTileDelegate.setAchievement(data)

            viewBinding.achievementTitle.text = achievementResourceResolver.resolveTitleForKind(data.kind)
            viewBinding.achievementDescription.text = achievementResourceResolver.resolveDescription(data)
        }
    }
}