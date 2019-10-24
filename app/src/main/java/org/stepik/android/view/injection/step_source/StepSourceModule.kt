package org.stepik.android.view.injection.step_source

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_source.EditStepSourcePresenter

@Module
internal abstract class StepSourceModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(EditStepSourcePresenter::class)
    internal abstract fun bindEditStepContentPresenter(editStepContentPresenter: EditStepSourcePresenter): ViewModel
}