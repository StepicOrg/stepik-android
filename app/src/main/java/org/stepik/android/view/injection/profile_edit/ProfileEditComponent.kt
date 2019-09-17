package org.stepik.android.view.injection.profile_edit

import dagger.Subcomponent
import org.stepik.android.view.injection.email_address.EmailAddressDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditActivity
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditInfoActivity
import org.stepik.android.view.profile_edit.ui.activity.ProfileEditPasswordActivity

@Subcomponent(modules = [
    ProfileDataModule::class,
    ProfileEditModule::class,
    EmailAddressDataModule::class
])
interface ProfileEditComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileEditComponent
    }

    fun inject(profileEditNavigationActivity: ProfileEditActivity)
    fun inject(profileEditInfoActivity: ProfileEditInfoActivity)
    fun inject(profileEditPasswordActivity: ProfileEditPasswordActivity)
}