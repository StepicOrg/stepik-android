package org.stepic.droid.di.step.code

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CodeStepFragment

@CodeScope
@Subcomponent
interface CodeComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CodeComponent
    }

    fun inject(codeStepFragment: CodeStepFragment)
}
