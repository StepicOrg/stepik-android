package org.stepic.droid.di.routing

import dagger.Subcomponent
import org.stepic.droid.di.section.SectionComponent
import org.stepic.droid.di.step.StepComponent

@RoutingScope
@Subcomponent
interface RoutingComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): RoutingComponent
    }

    fun stepComponentBuilder(): StepComponent.Builder

    fun sectionComponentBuilder(): SectionComponent.Builder

    //without injection, it is middle-man component
}
