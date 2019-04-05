package org.stepik.android.domain.profile.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.user.Profile

interface ProfileRepository {
    /**
     * Returns current profile
     */
    fun getProfile(primarySourceType: DataSourceType = DataSourceType.CACHE): Single<Profile>

    /**
     * Updates profile data
     */
    fun saveProfile(profile: Profile): Single<Profile>

    /**
     * Updates profile password
     */
    fun saveProfilePassword(profileId: Long, currentPassword: String, newPassword: String): Completable
}