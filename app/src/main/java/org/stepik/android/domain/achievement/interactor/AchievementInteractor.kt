package org.stepik.android.domain.achievement.interactor

import io.reactivex.Maybe
import io.reactivex.Observable
import org.stepic.droid.model.AchievementFlatItem
import org.stepik.android.domain.achievement.repository.AchievementRepository
import org.stepik.android.domain.profile.model.ProfileData
import javax.inject.Inject

class AchievementInteractor
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val achievementRepository: AchievementRepository
) {
    fun getUserAchievements(count: Int): Maybe<Pair<List<AchievementFlatItem>, Boolean>> =
        profileDataObservable
            .firstElement()
            .filter { !it.user.isOrganization && !it.user.isPrivate }
            .flatMapSingleElement { profileData ->
                achievementRepository
                    .getAchievements(profileData.user.id, count)
                    .map { it to profileData.isCurrentUser }
            }
}