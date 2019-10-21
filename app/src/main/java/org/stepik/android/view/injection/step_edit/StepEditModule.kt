package org.stepik.android.view.injection.step_edit

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_edit.EditStepContentPresenter

@Module
internal abstract class StepEditModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(EditStepContentPresenter::class)
    internal abstract fun bindEditStepContentPresenter(editStepContentPresenter: EditStepContentPresenter): ViewModel
}