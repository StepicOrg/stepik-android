package org.stepik.android.view.injection.step_quiz_text

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_quiz_text.TextStepQuizPresenter

@Module
abstract class TextStepQuizModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(TextStepQuizPresenter::class)
    internal abstract fun bindTextStepQuizPresenter(textStepQuizPresenter: TextStepQuizPresenter): ViewModel
}