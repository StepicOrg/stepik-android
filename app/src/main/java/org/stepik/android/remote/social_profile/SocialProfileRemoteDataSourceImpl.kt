package org.stepik.android.remote.social_profile

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.social_profile.source.SocialProfileRemoteDataSource
import org.stepik.android.model.SocialProfile
import org.stepik.android.remote.social_profile.model.SocialProfilesResponse
import org.stepik.android.remote.social_profile.service.SocialProfilesService
import javax.inject.Inject

class SocialProfileRemoteDataSourceImpl
@Inject
constructor(
    private val socialProfilesService: SocialProfilesService
) : SocialProfileRemoteDataSource {
    private val socialProfileResponseMapper =
        Function<SocialProfilesResponse, List<SocialProfile>>(SocialProfilesResponse::socialProfiles)

    override fun getSocialProfiles(vararg socialProfileIds: Long): Single<List<SocialProfile>> =
        socialProfilesService.getSocialProfiles(socialProfileIds).map(socialProfileResponseMapper)
}