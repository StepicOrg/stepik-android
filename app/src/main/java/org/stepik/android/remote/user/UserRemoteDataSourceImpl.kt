package org.stepik.android.remote.user

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.model.user.User
import javax.inject.Inject

class UserRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : UserRemoteDataSource {
    override fun getUsers(vararg userIds: Long): Single<List<User>> =
        api.getUsersRx(userIds)
}