package org.stepik.android.presentation.profile

import org.stepik.android.domain.profile.interactor.ProfileInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfilePresenter
@Inject
constructor(
    private val profileInteractor: ProfileInteractor
) : PresenterBase<ProfileView>() {

}