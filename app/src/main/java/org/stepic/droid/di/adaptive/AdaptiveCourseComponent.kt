package org.stepic.droid.di.adaptive

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment
import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.core.presenters.CardPresenter
import org.stepic.droid.di.qualifiers.CourseId

@AdaptiveCourseScope
@Subcomponent(modules = arrayOf(AdaptiveCourseModule::class))
interface AdaptiveCourseComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AdaptiveCourseComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(card: Card)

    fun inject(cardPresenter: CardPresenter)

    fun inject(recommendationsFragment: RecommendationsFragment)

    fun inject(adaptiveRatingFragment: AdaptiveRatingFragment)
}