package org.stepic.droid.di.routing

import dagger.Subcomponent
import org.stepic.droid.di.step.StepComponent

@RoutingScope
@Subcomponent
interface RoutingComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): RoutingComponent
    }

    fun stepComponentBuilder(): StepComponent.Builder

    //without injection, it is middle-man component
}
