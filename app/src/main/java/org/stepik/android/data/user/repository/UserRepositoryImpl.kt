package org.stepik.android.data.user.repository

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepik.android.data.user.source.UserCacheDataSource
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userCacheDataSource: UserCacheDataSource
) : UserRepository {
    override fun getUsers(vararg userIds: Long): Single<List<User>> =
        userCacheDataSource.getUsers(*userIds)
            .flatMap { cacheUsers ->
                val presentIds = cacheUsers.map(User::id).toLongArray()
                val emptyUsers = userIds.subtract(presentIds.asIterable()).toLongArray()

                zip(Single.just(cacheUsers), getUsersRemote(*emptyUsers))
            }.map { (cacheUsers, remoteUsers) ->
                (cacheUsers + remoteUsers).sortedBy { userIds.indexOf(it.id) }
            }

    private fun getUsersRemote(vararg userIds: Long): Single<List<User>> =
        userRemoteDataSource
            .getUsers(*userIds)
            .doOnSuccess { it.map(userCacheDataSource::saveUser).let(Completable::concat) }
}