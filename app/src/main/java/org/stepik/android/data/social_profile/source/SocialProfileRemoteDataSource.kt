package org.stepik.android.data.social_profile.source

import io.reactivex.Single
import org.stepik.android.model.SocialProfile

interface SocialProfileRemoteDataSource {
    fun getSocialProfiles(vararg socialProfileIds: Long): Single<List<SocialProfile>>
}