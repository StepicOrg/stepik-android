package org.stepik.android.domain.profile.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.Profile

interface ProfileRepository {
    /**
     * Returns current profile
     */
    fun getProfile(): Single<Profile>

    /**
     * Updates profile data
     */
    fun saveProfile(profile: Profile): Completable

    /**
     * Updates profile password
     */
    fun saveProfilePassword(currentPassword: String, newPassword: String)
}