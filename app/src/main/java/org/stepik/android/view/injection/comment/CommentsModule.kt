package org.stepik.android.view.injection.comment

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.comment.CommentsPresenter

@Module
internal abstract class CommentsModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CommentsPresenter::class)
    internal abstract fun bindCommentsPresenter(commentsPresenter: CommentsPresenter): ViewModel
}