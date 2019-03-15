package org.stepik.android.domain.profile_edit

import io.reactivex.Completable
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.model.user.Profile
import javax.inject.Inject

class ProfileEditInteractor
@Inject
constructor(
    private val profileRepository: ProfileRepository
) {
    fun updateProfile(profile: Profile): Completable =
        profileRepository
            .saveProfile(profile)
            .doOnSuccess {  } // todo post on bus
            .ignoreElement()

    fun updateProfilePassword(profileId: Long, currentPassword: String, newPassword: String): Completable =
        profileRepository
            .saveProfilePassword(profileId, currentPassword, newPassword)
}