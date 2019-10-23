package org.stepik.android.view.injection.comment

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.comment.ComposeCommentPresenter

@Module
internal abstract class ComposeCommentModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(ComposeCommentPresenter::class)
    internal abstract fun bindComposeCommentPresenter(composeCommentPresenter: ComposeCommentPresenter): ViewModel
}