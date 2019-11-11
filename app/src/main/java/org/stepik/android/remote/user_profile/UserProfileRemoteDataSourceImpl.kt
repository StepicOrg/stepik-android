package org.stepik.android.remote.user_profile

import io.reactivex.Single
import org.stepik.android.data.user_profile.source.UserProfileRemoteDataSource
import org.stepik.android.remote.auth.model.StepikProfileResponse
import org.stepik.android.remote.user_profile.service.UserProfileService
import javax.inject.Inject

class UserProfileRemoteDataSourceImpl
@Inject
constructor(
    private val userProfileService: UserProfileService
) : UserProfileRemoteDataSource {
    override fun getUserProfile(): Single<StepikProfileResponse> =
        userProfileService.getUserProfile()
}