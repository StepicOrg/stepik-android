package org.stepik.android.view.injection.feedback

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.feedback.FeedbackPresenter

@Module
abstract class FeedbackModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(FeedbackPresenter::class)
    internal abstract fun bindFeedbackPresenter(feedbackPresenter: FeedbackPresenter): ViewModel
}