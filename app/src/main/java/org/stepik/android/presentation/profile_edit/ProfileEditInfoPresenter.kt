package org.stepik.android.presentation.profile_edit

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.base.PresenterBase
import retrofit2.HttpException
import javax.inject.Inject

class ProfileEditInfoPresenter
@Inject
constructor(
    private val profileEditInteractor: ProfileEditInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileEditInfoView>() {
    private var state = ProfileEditInfoView.State.IDLE
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: ProfileEditInfoView) {
        super.attachView(view)
        view.setState(state)
    }

    fun updateProfileInfo(
        profile: Profile,
        firstName: String, lastName: String,
        shortBio: String, details: String
    ) {
        if (state != ProfileEditInfoView.State.IDLE) return

        if (profile.firstName == firstName &&
            profile.lastName == lastName &&
            profile.shortBio == shortBio &&
            profile.details == details
        ) {
            state = ProfileEditInfoView.State.COMPLETE
            return
        }

        state = ProfileEditInfoView.State.LOADING
        val newProfile = profile
            .copy(
                firstName = firstName,
                lastName = lastName,
                shortBio = shortBio,
                details = details
            )
        compositeDisposable += profileEditInteractor
            .updateProfile(newProfile)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = { state = ProfileEditInfoView.State.COMPLETE },
                onError = {
                    state = ProfileEditInfoView.State.IDLE
                    if (it is HttpException) {
                        view?.showInfoError()
                    } else {
                        view?.showNetworkError()
                    }
                }
            )
    }

}