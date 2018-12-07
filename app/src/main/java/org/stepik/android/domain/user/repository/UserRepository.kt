package org.stepik.android.domain.user.repository

import io.reactivex.Single
import org.stepik.android.model.user.User

interface UserRepository {
    fun getUsers(vararg userIds: Long): Single<List<User>>
}