package org.stepik.android.domain.user.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.user.User

interface UserRepository {
    fun getUsers(vararg userIds: Long, primarySourceType: DataSourceType = DataSourceType.REMOTE): Single<List<User>>
}