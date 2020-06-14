package org.stepik.android.data.user_profile.source

import io.reactivex.Single
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.User

interface UserProfileRemoteDataSource {
    fun getUserProfile(): Single<Pair<User?, Profile?>>
}