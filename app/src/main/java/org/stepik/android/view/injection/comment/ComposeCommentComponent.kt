package org.stepik.android.view.injection.comment

import dagger.Subcomponent
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import org.stepik.android.view.injection.submission.SubmissionDataModule

@Subcomponent(modules = [
    CommentDataModule::class,
    ComposeCommentModule::class,
    SubmissionDataModule::class
])
interface ComposeCommentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ComposeCommentComponent
    }

    fun inject(composeCommentDialogFragment: ComposeCommentDialogFragment)
}