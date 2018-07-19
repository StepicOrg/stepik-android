package org.stepic.droid.features.achievements.ui.adapters.holders

import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.view_achievement_item.view.*
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.ui.adapters.viewhoders.GenericViewHolder

class AchievementViewHolder(
        root: View,
        private val achievementResourceResolver: AchievementResourceResolver
): GenericViewHolder<AchievementFlatItem>(root) {
    private val achievementTitle: TextView = root.achievementTitle
    private val achievementDescription: TextView = root.achievementDescription

    private val achievementTileViewHolder = AchievementTileViewHolder(root.achievementTile, achievementResourceResolver)

    override fun onBind(item: AchievementFlatItem) {
        achievementTileViewHolder.onBind(item)

        achievementTitle.text = achievementResourceResolver.resolveTitleForKind(item.kind)
        achievementDescription.text = achievementResourceResolver.resolveDescription(item)
    }
}