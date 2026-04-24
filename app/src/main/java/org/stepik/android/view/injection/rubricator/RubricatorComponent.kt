package org.stepik.android.view.injection.rubricator

import dagger.Subcomponent
import org.stepik.android.view.rubricator.ui.RubricatorActivity

@Subcomponent(modules = [
    RubricatorPresentationModule::class,
    RubricatorDataModule::class
])
interface RubricatorComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): RubricatorComponent
    }

    fun inject(rubricatorActivity: RubricatorActivity)
}