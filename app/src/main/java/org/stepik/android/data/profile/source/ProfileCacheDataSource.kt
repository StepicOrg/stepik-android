package org.stepik.android.data.profile.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.model.user.Profile

interface ProfileCacheDataSource {
    /**
     * Returns current profile
     */
    fun getProfile(): Maybe<Profile>

    /**
     * Updates profile data
     */
    fun saveProfile(profile: Profile): Completable
}