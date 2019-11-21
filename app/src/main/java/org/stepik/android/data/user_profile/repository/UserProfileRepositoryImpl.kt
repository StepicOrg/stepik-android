package org.stepik.android.data.user_profile.repository

import io.reactivex.Single
import org.stepik.android.data.user_profile.source.UserProfileRemoteDataSource
import org.stepik.android.domain.user_profile.repository.UserProfileRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.User
import javax.inject.Inject

class UserProfileRepositoryImpl
@Inject
constructor(
    private val userProfileRemoteDataSource: UserProfileRemoteDataSource
) : UserProfileRepository {
    override fun getUserProfile(): Single<Pair<User?, Profile?>> =
        userProfileRemoteDataSource.getUserProfile()
}