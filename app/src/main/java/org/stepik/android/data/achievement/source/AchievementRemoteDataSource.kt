package org.stepik.android.data.achievement.source

import io.reactivex.Single
import org.stepik.android.domain.achievement.model.AchievementItem

interface AchievementRemoteDataSource {
    fun getAchievements(userId: Long, count: Int = -1): Single<List<AchievementItem>>
    fun getAchievement(userId: Long, kind: String): Single<AchievementItem>
}