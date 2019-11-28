package org.stepik.android.domain.profile.interactor

import io.reactivex.Maybe
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import javax.inject.Inject

class ProfileInteractor
@Inject
constructor(
    private val userRepository: UserRepository
) {
    fun getUser(userId: Long): Maybe<User> =
        userRepository
            .getUser(userId)
}