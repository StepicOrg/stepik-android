package org.stepik.android.domain.achievement.repository

import io.reactivex.Single
import org.stepic.droid.model.AchievementFlatItem

interface AchievementRepository {
    fun getAchievements(userId: Long, count: Int = -1): Single<List<AchievementFlatItem>>
    fun getAchievement(userId: Long, kind: String): Single<AchievementFlatItem>
}