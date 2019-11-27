package org.stepik.android.data.achievements.source

import io.reactivex.Single
import org.stepic.droid.model.AchievementFlatItem

interface AchievementsRemoteDataSource {
    fun getAchievements(userId: Long, count: Int = -1): Single<List<AchievementFlatItem>>
    fun getAchievement(userId: Long, kind: String): Single<AchievementFlatItem>
}