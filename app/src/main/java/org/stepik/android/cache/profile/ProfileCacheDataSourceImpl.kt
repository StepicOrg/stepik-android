package org.stepik.android.cache.profile

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.toMaybe
import org.stepik.android.data.profile.source.ProfileCacheDataSource
import org.stepik.android.model.user.Profile
import javax.inject.Inject

class ProfileCacheDataSourceImpl
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : ProfileCacheDataSource {
    override fun getProfile(): Maybe<Profile> =
        sharedPreferenceHelper
            .profile
            .toMaybe()

    override fun saveProfile(profile: Profile): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.storeProfile(profile)
        }
}