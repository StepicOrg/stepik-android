package org.stepik.android.data.achievements.repository

import io.reactivex.Single
import org.stepic.droid.model.AchievementFlatItem
import org.stepik.android.data.achievements.source.AchievementsRemoteDataSource
import org.stepik.android.domain.achievements.repository.AchievementsRepository
import javax.inject.Inject

class AchievementsRepositoryImpl
@Inject
constructor(
    private val achievementsRemoteDataSource: AchievementsRemoteDataSource
) : AchievementsRepository {

    override fun getAchievements(userId: Long, count: Int): Single<List<AchievementFlatItem>> =
        achievementsRemoteDataSource.getAchievements(userId, count)

    override fun getAchievement(userId: Long, kind: String): Single<AchievementFlatItem> =
        achievementsRemoteDataSource.getAchievement(userId, kind)
}