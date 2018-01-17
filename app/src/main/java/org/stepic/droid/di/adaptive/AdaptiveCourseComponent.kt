package org.stepic.droid.di.adaptive

import dagger.Subcomponent
import org.stepic.droid.adaptive.model.Card

@AdaptiveCourseScope
@Subcomponent(modules = arrayOf(AdaptiveCourseModule::class))
interface AdaptiveCourseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AdaptiveCourseComponent
    }

    fun inject(card: Card)
}