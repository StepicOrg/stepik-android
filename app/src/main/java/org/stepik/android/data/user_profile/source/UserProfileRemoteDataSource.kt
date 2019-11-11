package org.stepik.android.data.user_profile.source

import io.reactivex.Single
import org.stepik.android.remote.auth.model.StepikProfileResponse

interface UserProfileRemoteDataSource {
    fun getUserProfile(): Single<StepikProfileResponse>
}