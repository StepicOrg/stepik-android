package org.stepik.android.presentation.profile_edit

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.presentation.base.PresenterBase
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

    fun updateProfilePassword(profileId: Long, currentPassword: String, newPassword: String) {
        compositeDisposable += profileEditInteractor
            .updateProfilePassword(profileId, currentPassword, newPassword)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy()
    }
}