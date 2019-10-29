package org.stepik.android.view.injection.comment

import dagger.Subcomponent
import org.stepik.android.view.comment.ui.activity.CommentsActivity
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import org.stepik.android.view.injection.discussion_proxy.DiscussionProxyDataModule
import org.stepik.android.view.injection.vote.VoteDataModule

@Subcomponent(modules = [
    CommentsModule::class,

    CommentDataModule::class,
    DiscussionProxyDataModule::class,
    VoteDataModule::class
])
interface CommentsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CommentsComponent
    }

    fun inject(commentsActivity: CommentsActivity)
    fun inject(solutionCommentDialogFragment: SolutionCommentDialogFragment)
}