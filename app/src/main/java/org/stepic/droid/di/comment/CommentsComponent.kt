package org.stepic.droid.di.comment

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CommentsFragment

@CommentsScope
@Subcomponent(modules = arrayOf(CommentsModule::class))
interface CommentsComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CommentsComponent
    }

    fun inject(commentsFragment: CommentsFragment)
}
