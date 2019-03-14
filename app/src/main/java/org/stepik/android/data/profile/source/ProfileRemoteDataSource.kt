package org.stepik.android.data.profile.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.Profile

interface ProfileRemoteDataSource {
    /**
     * Returns current profile
     */
    fun getProfile(): Single<Profile>

    /**
     * Updates profile data
     */
    fun saveProfile(profile: Profile): Single<Profile>

    /**
     * Updates profile password
     */
    fun saveProfilePassword(currentPassword: String, newPassword: String): Completable
}