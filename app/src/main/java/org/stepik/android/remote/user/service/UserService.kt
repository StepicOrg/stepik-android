package org.stepik.android.remote.user.service

import io.reactivex.Single
import org.stepik.android.remote.user.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {
    @GET("api/users")
    fun getUsersRx(@Query("ids[]") userIds: List<Long>): Single<UserResponse>
}