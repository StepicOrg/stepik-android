package org.stepik.android.domain.profile_edit

import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.model.user.Profile
import javax.inject.Inject

class ProfileEditInteractor
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val profileSubject: PublishSubject<Profile>
) {
    fun updateProfile(profile: Profile): Completable =
        profileRepository
            .saveProfile(profile)
            .doOnSuccess(profileSubject::onNext)
            .ignoreElement()

    fun updateProfilePassword(profileId: Long, currentPassword: String, newPassword: String): Completable =
        profileRepository
            .saveProfilePassword(profileId, currentPassword, newPassword)
}