package org.stepik.android.presentation.profile_edit

import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileEditPasswordPresenter
@Inject
constructor(
    private val profileEditInteractor: ProfileEditInteractor
) : PresenterBase<ProfileEditPasswordView>() {

}