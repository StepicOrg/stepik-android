package org.stepik.android.cache.social_profile

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.social_profile.structure.DbStructureSocialProfile
import org.stepik.android.data.social_profile.source.SocialProfileCacheDataSource
import org.stepik.android.model.SocialProfile
import javax.inject.Inject

class SocialProfileCacheDataSourceImpl
@Inject
constructor(
    private val socialProfileDao: IDao<SocialProfile>
) : SocialProfileCacheDataSource {
    override fun getSocialProfiles(vararg socialProfileIds: Long): Single<List<SocialProfile>> =
        Single.fromCallable {
            socialProfileDao.getAllInRange(DbStructureSocialProfile.Columns.ID, socialProfileIds.joinToString())
        }

    override fun saveSocialProfiles(socialProfiles: List<SocialProfile>): Completable =
        Completable.fromAction {
            socialProfileDao.insertOrReplaceAll(socialProfiles)
        }
}