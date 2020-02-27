package org.stepik.android.view.injection.user_code_run

import dagger.Subcomponent
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment

@Subcomponent(modules = [
    UserCodeRunModule::class,
    UserDataModule::class,
    ProfileDataModule::class
])
interface UserCodeRunComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): UserCodeRunComponent
    }

    fun inject(codeStepQuizFullScreenDialogFragment: CodeStepQuizFullScreenDialogFragment)
}