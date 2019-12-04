package org.stepik.android.data.achievement.repository

import io.reactivex.Single
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.data.achievement.source.AchievementRemoteDataSource
import org.stepik.android.domain.achievement.repository.AchievementRepository
import javax.inject.Inject

class AchievementRepositoryImpl
@Inject
constructor(
    private val achievementRemoteDataSource: AchievementRemoteDataSource
) : AchievementRepository {

    override fun getAchievements(userId: Long, count: Int): Single<List<AchievementItem>> =
        achievementRemoteDataSource.getAchievements(userId, count)

    override fun getAchievement(userId: Long, kind: String): Single<AchievementItem> =
        achievementRemoteDataSource.getAchievement(userId, kind)
}