package org.stepik.android.domain.user.repository

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.user.User

interface UserRepository {
    fun getUsers(vararg userIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<User>>

    fun getUser(userId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<User> =
        getUsers(userId, primarySourceType = primarySourceType)
            .maybeFirst()
}