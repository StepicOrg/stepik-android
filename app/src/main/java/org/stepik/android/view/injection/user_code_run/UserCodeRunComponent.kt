package org.stepik.android.view.injection.user_code_run

import dagger.Subcomponent
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment

@Subcomponent(modules = [
    UserCodeRunModule::class,
    UserDataModule::class,
    UserProfileDataModule::class
])
interface UserCodeRunComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): UserCodeRunComponent
    }

    fun inject(codeStepQuizFullScreenDialogFragment: CodeStepQuizFullScreenDialogFragment)
}