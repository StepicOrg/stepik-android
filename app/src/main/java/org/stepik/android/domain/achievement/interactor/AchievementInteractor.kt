package org.stepik.android.domain.achievement.interactor

import io.reactivex.Single
import org.stepik.android.domain.achievement.model.AchievementItem
import org.stepik.android.domain.achievement.repository.AchievementRepository
import javax.inject.Inject

class AchievementInteractor
@Inject
constructor(
    private val achievementRepository: AchievementRepository
) {
    fun getAchievements(userId: Long, count: Int = -1): Single<List<AchievementItem>> =
        achievementRepository
            .getAchievements(userId, count)
}