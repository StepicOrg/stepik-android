package org.stepik.android.remote.user

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.model.user.User
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.user.model.UserResponse
import javax.inject.Inject

class UserRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : UserRemoteDataSource {
    private val userResponseMapper =
        Function<UserResponse, List<User>>(UserResponse::users)

    override fun getUsers(vararg userIds: Long): Single<List<User>> =
        userIds
            .chunkedSingleMap { ids ->
                api.getUsersRx(ids)
                    .map(userResponseMapper)
            }
}