package org.stepik.android.domain.user_code_run.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepik.android.domain.user_code_run.repository.UserCodeRunRepository
import org.stepik.android.domain.user_profile.repository.UserProfileRepository
import org.stepik.android.model.code.UserCodeRun
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserCodeRunInteractor
@Inject
constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userCodeRunRepository: UserCodeRunRepository
) {
    fun createUserCodeRun(userCodeRun: UserCodeRun): Single<UserCodeRun> =
        userProfileRepository
            .getUserProfile()
            .flatMap { userProfile ->
                val userId = userProfile.first?.id ?: 0
                // TODO Use user id
                userCodeRunRepository
                    .createUserCodeRun(userCodeRun)
                    .flatMapObservable { createdUserCodeRun ->
                        Observable
                            .interval(1, TimeUnit.SECONDS)
                            .flatMapSingle { userCodeRunRepository.getUserCodeRun(createdUserCodeRun.id) }
                            .skipWhile { userCodeRun.status == UserCodeRun.Status.EVALUATION}
                    }
                    .firstOrError()
            }

}