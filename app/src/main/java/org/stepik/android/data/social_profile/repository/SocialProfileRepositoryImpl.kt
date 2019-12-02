package org.stepik.android.data.social_profile.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.social_profile.source.SocialProfileCacheDataSource
import org.stepik.android.data.social_profile.source.SocialProfileRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.social_profile.repository.SocialProfileRepository
import org.stepik.android.model.SocialProfile
import javax.inject.Inject

class SocialProfileRepositoryImpl
@Inject
constructor(
    private val socialProfileRemoteDataSource: SocialProfileRemoteDataSource,
    private val socialProfileCacheDataSource: SocialProfileCacheDataSource
) : SocialProfileRepository {
    override fun getSocialProfiles(vararg socialProfileIds: Long, sourceType: DataSourceType): Single<List<SocialProfile>> =
        when (sourceType) {
            DataSourceType.CACHE ->
                socialProfileCacheDataSource
                    .getSocialProfiles(*socialProfileIds)

            DataSourceType.REMOTE ->
                socialProfileRemoteDataSource
                    .getSocialProfiles(*socialProfileIds)
                    .doCompletableOnSuccess(socialProfileCacheDataSource::saveSocialProfiles)

            else -> throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
}