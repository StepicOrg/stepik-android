package org.stepik.android.domain.social_profile.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.SocialProfile

interface SocialProfileRepository {
    fun getSocialProfiles(vararg socialProfileIds: Long, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<SocialProfile>>
}