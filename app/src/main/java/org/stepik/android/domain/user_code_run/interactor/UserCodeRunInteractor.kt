package org.stepik.android.domain.user_code_run.interactor

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_code_run.repository.UserCodeRunRepository
import org.stepik.android.model.code.UserCodeRun
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UserCodeRunInteractor
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val profileRepository: ProfileRepository,
    private val userCodeRunRepository: UserCodeRunRepository
) {
    fun createUserCodeRun(code: String, language: String, stdin: String, stepId: Long): Single<UserCodeRun> =
        profileRepository
            .getProfile()
            .flatMap { profile ->
                userCodeRunRepository
                    .createUserCodeRun(
                        UserCodeRun(
                            code = code,
                            language = language,
                            stdin = stdin,
                            step = stepId,
                            user = profile.id
                        )
                    )
                    .flatMapObservable { createdUserCodeRun ->
                        Observable
                            .interval(1, TimeUnit.SECONDS)
                            .flatMapSingle { userCodeRunRepository.getUserCodeRun(createdUserCodeRun.id) }
                            .skipWhile { it.status == UserCodeRun.Status.EVALUATION }
                    }
                    .firstOrError()
            }

    fun isRunCodePopupShown(): Single<Boolean> =
        Single.fromCallable {
            val isRunCodePopupShown = sharedPreferenceHelper.isRunCodePopupShown
            sharedPreferenceHelper.afterRunCodePopupShown()

            isRunCodePopupShown
        }
}
