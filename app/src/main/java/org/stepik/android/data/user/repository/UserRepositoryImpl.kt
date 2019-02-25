package org.stepik.android.data.user.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.user.source.UserCacheDataSource
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userCacheDataSource: UserCacheDataSource
) : UserRepository {
    override fun getUsers(vararg userIds: Long, primarySourceType: DataSourceType): Single<List<User>> {
        val remoteSource = userRemoteDataSource
            .getUsers(*userIds)
            .doCompletableOnSuccess(userCacheDataSource::saveUsers)

        val cacheSource = userCacheDataSource
            .getUsers(*userIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedUsers ->
                    val ids = (userIds.toList() - cachedUsers.map(User::id)).toLongArray()
                    userRemoteDataSource
                        .getUsers(*ids)
                        .doCompletableOnSuccess(userCacheDataSource::saveUsers)
                        .map { remoteUsers -> cachedUsers + remoteUsers }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { users -> users.sortedBy { userIds.indexOf(it.id) } }
    }
}