package org.stepic.droid.di.adaptive

import dagger.Subcomponent
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.core.presenters.CardPresenter

@AdaptiveCourseScope
@Subcomponent(modules = arrayOf(AdaptiveCourseModule::class))
interface AdaptiveCourseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AdaptiveCourseComponent
    }

    fun inject(card: Card)

    fun inject(cardPresenter: CardPresenter)

    fun inject(recommendationsFragment: RecommendationsFragment)
}