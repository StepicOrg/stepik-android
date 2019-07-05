package org.stepic.droid.di.comment

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CommentsFragment
import org.stepik.android.view.injection.comment.CommentDataModule

@CommentsScope
@Subcomponent(modules = [
    CommentsModule::class,
    CommentDataModule::class
])
interface CommentsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CommentsComponent
    }

    fun inject(commentsFragment: CommentsFragment)
}
