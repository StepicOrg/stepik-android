package org.stepik.android.presentation.profile_edit

import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileEditInfoPresenter
@Inject
constructor(
    private val profileEditInteractor: ProfileEditInteractor
) : PresenterBase<ProfileEditInfoView>() {



}