package org.stepik.android.data.profile.repository

import io.reactivex.Completable
import io.reactivex.Single
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import org.stepik.android.data.profile.source.ProfileCacheDataSource
import org.stepik.android.data.profile.source.ProfileRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.model.user.Profile
import javax.inject.Inject

class ProfileRepositoryImpl
@Inject
constructor(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
    private val profileCacheDataSource: ProfileCacheDataSource
) : ProfileRepository {

    override fun getProfile(primarySourceType: DataSourceType, canFallback: Boolean): Single<Profile> {
        val remoteSource = profileRemoteDataSource
            .getProfile()
            .doCompletableOnSuccess(profileCacheDataSource::saveProfile)

        val cacheSource = profileCacheDataSource
            .getProfile()

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                if (canFallback) {
                    remoteSource.onErrorResumeNext(cacheSource.toSingle())
                } else {
                    remoteSource
                }

            DataSourceType.CACHE ->
                if (canFallback) {
                    cacheSource.switchIfEmpty(remoteSource)
                } else {
                    cacheSource.toSingle()
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

    override fun saveProfile(profile: Profile): Single<Profile> =
        profileRemoteDataSource
            .saveProfile(profile)
            .doCompletableOnSuccess(profileCacheDataSource::saveProfile)

    override fun saveProfilePassword(profileId: Long, currentPassword: String, newPassword: String): Completable =
        profileRemoteDataSource
            .saveProfilePassword(profileId, currentPassword, newPassword)
}