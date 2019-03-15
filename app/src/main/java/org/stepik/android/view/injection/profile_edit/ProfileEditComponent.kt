package org.stepik.android.view.injection.profile_edit

import dagger.Subcomponent
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditInfoActivity
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditPasswordActivity

@Subcomponent(modules = [
    ProfileDataModule::class,
    ProfileEditModule::class
])
interface ProfileEditComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileEditComponent
    }

    fun inject(profileEditInfoActivity: ProfileEditInfoActivity)
    fun inject(profileEditPasswordActivity: ProfileEditPasswordActivity)
}