package org.stepik.android.remote.profile

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.profile.source.ProfileRemoteDataSource
import org.stepik.android.model.user.Profile
import org.stepik.android.remote.profile.model.ProfilePasswordRequest
import org.stepik.android.remote.profile.model.ProfileRequest
import org.stepik.android.remote.profile.service.ProfileService
import javax.inject.Inject

class ProfileRemoteDataSourceImpl
@Inject
constructor(
    private val profileService: ProfileService
) : ProfileRemoteDataSource {
    override fun getProfile(): Single<Profile> =
        profileService
            .getProfile()
            .map { it.profiles.first() }

    override fun saveProfile(profile: Profile): Single<Profile> =
        profileService
            .saveProfile(profile.id, ProfileRequest(profile))
            .map { it.profiles.first() }

    override fun saveProfilePassword(profileId: Long, currentPassword: String, newPassword: String): Completable =
        profileService
            .saveProfilePassword(profileId, ProfilePasswordRequest(currentPassword, newPassword))
}