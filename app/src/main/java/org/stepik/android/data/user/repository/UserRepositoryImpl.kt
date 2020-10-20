package org.stepik.android.data.user.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
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
    private val delegate =
        ListRepositoryDelegate(
            userRemoteDataSource::getUsers,
            userCacheDataSource::getUsers,
            userCacheDataSource::saveUsers
        )

    override fun getUsers(userIds: List<Long>, primarySourceType: DataSourceType): Single<List<User>> =
        delegate.get(userIds, primarySourceType, allowFallback = true)
}