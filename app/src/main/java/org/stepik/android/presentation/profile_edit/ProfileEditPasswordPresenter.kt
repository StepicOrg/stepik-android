package org.stepik.android.presentation.profile_edit

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.presentation.base.PresenterBase
import retrofit2.HttpException
import javax.inject.Inject

class ProfileEditPasswordPresenter
@Inject
constructor(
    private val profileEditInteractor: ProfileEditInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileEditPasswordView>() {
    private var state = ProfileEditPasswordView.State.IDLE
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: ProfileEditPasswordView) {
        super.attachView(view)
        view.setState(state)
    }

    fun updateProfilePassword(profileId: Long, currentPassword: String, newPassword: String) {
        if (state != ProfileEditPasswordView.State.IDLE) return

        state = ProfileEditPasswordView.State.LOADING
        compositeDisposable += profileEditInteractor
            .updateProfilePassword(profileId, currentPassword, newPassword)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = ProfileEditPasswordView.State.COMPLETE },
                onError = {
                    state = ProfileEditPasswordView.State.IDLE
                    if (it is HttpException) {
                        view?.showPasswordError()
                    } else {
                        view?.showNetworkError()
                    }
                }
            )
    }
}