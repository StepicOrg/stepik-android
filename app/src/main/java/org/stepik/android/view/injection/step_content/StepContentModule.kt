package org.stepik.android.view.injection.step_content

import dagger.Binds
import dagger.Module
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactory
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactoryImpl

@Module
abstract class StepContentModule {
    @Binds
    internal abstract fun bindStepContenntFragmentFactory(
        stepContentFragmentFactoryImpl: StepContentFragmentFactoryImpl
    ): StepContentFragmentFactory
}