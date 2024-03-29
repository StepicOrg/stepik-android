package org.stepik.android.domain.settings.interactor

import io.reactivex.Single
import org.stepic.droid.preferences.UserPreferences
import org.stepik.android.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class SettingsAccountDeletionInteractor
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val userPreferences: UserPreferences
) {
    fun isCurrentAccountWasDeleted(): Single<Boolean> =
        profileRepository
            .getProfileFromRemoteWithoutCaching()
            .map { profile ->
                /**
                 * If account was deleted
                 * then `getProfile` will return new anonymous user with different id
                 */
                userPreferences.userId != profile.id
            }
}