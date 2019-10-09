package org.stepik.android.view.injection.step

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step.StepPresenter

@Module
abstract class StepModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(StepPresenter::class)
    internal abstract fun bindStepPresenter(stepPresenter: StepPresenter): ViewModel
}