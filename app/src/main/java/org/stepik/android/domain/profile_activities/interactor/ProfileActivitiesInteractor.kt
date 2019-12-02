package org.stepik.android.domain.profile_activities.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.UserActivity
import javax.inject.Inject

class ProfileActivitiesInteractor
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val userActivityRepository: UserActivityRepository
) {
    fun getUserActivities(): Single<List<Long>> =
        profileDataObservable
            .firstOrError()
            .flatMap { profileData ->
                userActivityRepository
                    .getUserActivities(profileData.user.id)
            }
            .maybeFirst()
            .toSingle()
            .map(UserActivity::pins)
}