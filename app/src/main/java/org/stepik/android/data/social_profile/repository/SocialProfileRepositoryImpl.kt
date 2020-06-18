package org.stepik.android.data.social_profile.repository

import io.reactivex.Single
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import ru.nobird.android.domain.rx.requireSize
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
    override fun getSocialProfiles(vararg socialProfileIds: Long, sourceType: DataSourceType): Single<List<SocialProfile>> {

        val cacheSource = socialProfileCacheDataSource.getSocialProfiles(*socialProfileIds)

        val remoteSource = socialProfileRemoteDataSource
            .getSocialProfiles(*socialProfileIds)
            .doCompletableOnSuccess(socialProfileCacheDataSource::saveSocialProfiles)

        return when (sourceType) {
            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedSocialProfiles ->
                    val ids = (socialProfileIds.toList() - cachedSocialProfiles.map(SocialProfile::id)).toLongArray()
                    if (ids.isNotEmpty()) {
                        socialProfileRemoteDataSource
                            .getSocialProfiles(*ids)
                            .doCompletableOnSuccess(socialProfileCacheDataSource::saveSocialProfiles)
                            .map { remoteSocialProfiles -> cachedSocialProfiles + remoteSocialProfiles }
                    } else {
                        Single.just(cachedSocialProfiles)
                    }
                }

            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(socialProfileIds.size))

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }.map { socialProfiles -> socialProfiles.sortedBy { socialProfileIds.indexOf(it.id) } }
    }
}