package org.stepik.android.view.injection.submission

import dagger.Subcomponent
import org.stepik.android.view.injection.attempt.AttemptDataModule
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment

@Subcomponent(modules = [
    SubmissionModule::class,
    SubmissionDataModule::class,
    AttemptDataModule::class
])
interface SubmissionComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SubmissionComponent
    }

    fun inject(submissionsFragmentDialog: SubmissionsDialogFragment)
}