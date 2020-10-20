package org.stepik.android.remote.user

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.model.user.User
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.user.model.UserResponse
import org.stepik.android.remote.user.service.UserService
import javax.inject.Inject

class UserRemoteDataSourceImpl
@Inject
constructor(
    private val userService: UserService
) : UserRemoteDataSource {
    private val userResponseMapper =
        Function<UserResponse, List<User>>(UserResponse::users)

    override fun getUsers(userIds: List<Long>): Single<List<User>> =
        userIds
            .chunkedSingleMap { ids ->
                userService.getUsersRx(ids)
                    .map(userResponseMapper)
            }
}