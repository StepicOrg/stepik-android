package org.stepik.android.view.injection.comment

import dagger.Subcomponent
import org.stepik.android.view.comment.ui.activity.CommentsActivity

@Subcomponent(modules = [
    CommentDataModule::class,
    CommentsModule::class
])
interface CommentsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CommentsComponent
    }

    fun inject(commentsActivity: CommentsActivity)
}