package org.stepic.droid.di.adaptive

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.adaptive.model.Card
import org.stepic.droid.adaptive.ui.fragments.AdaptiveProgressFragment
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment
import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.core.presenters.CardPresenter
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.injection.attempt.AttemptDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.recommendation.RecommendationModule
import org.stepik.android.view.injection.submission.SubmissionDataModule
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.view_assignment.ViewAssignmentDataModule

@AdaptiveCourseScope
@Subcomponent(modules = [
    AdaptiveCourseModule::class,
    LastStepDataModule::class,
    ViewAssignmentDataModule::class,
    SubmissionDataModule::class,
    AttemptDataModule::class,
    RecommendationModule::class,
    UserDataModule::class
])
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

    fun inject(adaptiveProgressFragment: AdaptiveProgressFragment)
}