package org.stepik.android.domain.user_profile.repository

import io.reactivex.Single
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.User

interface UserProfileRepository {
    fun getUserProfile(): Single<Pair<User?, Profile?>>
}