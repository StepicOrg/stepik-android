package org.stepic.droid.features.achievements.util

import android.content.Context
import android.widget.ImageView
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.model.achievements.AchievementFlatItem
import javax.inject.Inject

@AppSingleton
class AchievementResourceResolver
@Inject
constructor(
        private val context: Context
) {
    fun resolveTitleForKind(kind: String) = ""

    fun resolveDescriptionForKind(kind: String) = ""

    fun resolveAchievementIcon(achievementFlatItem: AchievementFlatItem, targetImageView: ImageView) = if (achievementFlatItem.isLocked) {
        "file:///android_asset/images/vector/achievements/ic_empty_achievement.svg"
    } else {
        "file:///android_asset/images/vector/achievements/ic_empty_achievement.svg"
//        "${achievementFlatItem.iconId ?: ""}/${targetImageView.width}x${targetImageView.height}" // todo: update after backend support
    }
}