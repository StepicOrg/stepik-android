package org.stepik.android.data.user.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.user.User

interface UserCacheDataSource {
    fun getUsers(vararg userIds: Long): Single<List<User>>
    fun saveUser(user: User): Completable
}