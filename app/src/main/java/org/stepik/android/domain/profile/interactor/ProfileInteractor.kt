package org.stepik.android.domain.profile.interactor

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.user.repository.UserRepository
import javax.inject.Inject

class ProfileInteractor
@Inject
constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) {
    fun getUser(userId: Long): Observable<ProfileData> =
        Single
            .fromCallable { userId.takeIf { it > 0 } ?: userPreferences.userId }
            .flatMapObservable { user ->
                Maybe
                    .concat(
                        userRepository.getUser(user, primarySourceType = DataSourceType.CACHE),
                        userRepository.getUser(user, primarySourceType = DataSourceType.REMOTE)
                    )
                    .toObservable()
            }
            .map { user ->
                ProfileData(user, user.id == userPreferences.userId)
            }
}