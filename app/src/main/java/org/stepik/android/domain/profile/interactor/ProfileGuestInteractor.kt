package org.stepik.android.domain.profile.interactor

import io.reactivex.Single
import org.stepik.android.domain.profile.repository.ProfileRepository
import javax.inject.Inject

class ProfileGuestInteractor
@Inject
constructor(
    private val profileRepository: ProfileRepository
) {
    fun isGuest(): Single<Boolean> =
        profileRepository
            .getProfile()
            .map { it.isGuest }
}