package org.stepik.android.domain.social_profile.interactor

import io.reactivex.Single
import org.stepik.android.domain.social_profile.repository.SocialProfileRepository
import org.stepik.android.model.SocialProfile
import javax.inject.Inject

class SocialProfileInteractor
@Inject
constructor(
    private val socialProfileRepository: SocialProfileRepository
) {
    fun getSocialProfiles(vararg socialProfileIds: Long): Single<List<SocialProfile>> =
        socialProfileRepository
            .getSocialProfiles(*socialProfileIds)
}