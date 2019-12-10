package org.stepik.android.data.social_profile.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.SocialProfile

interface SocialProfileCacheDataSource {
    fun getSocialProfiles(vararg socialProfileIds: Long): Single<List<SocialProfile>>
    fun saveSocialProfiles(socialProfiles: List<SocialProfile>): Completable
}