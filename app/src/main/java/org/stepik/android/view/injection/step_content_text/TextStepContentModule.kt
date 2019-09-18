package org.stepik.android.view.injection.step_content_text

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_content_text.TextStepContentPresenter

@Module
abstract class TextStepContentModule {
    @Binds
    @IntoMap
    @ViewModelKey(TextStepContentPresenter::class)
    internal abstract fun bindTextStepContentPresenter(textStepContentPresenter: TextStepContentPresenter): ViewModel
}