package org.stepik.android.data.user.source

import io.reactivex.Single
import org.stepik.android.model.user.User

interface UserRemoteDataSource {
    fun getUsers(userIds: List<Long>): Single<List<User>>
}