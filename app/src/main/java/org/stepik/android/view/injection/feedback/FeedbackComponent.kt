package org.stepik.android.view.injection.feedback

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.FeedbackFragment

@Subcomponent(modules = [FeedbackModule::class, FeedbackDataModule::class])
interface FeedbackComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FeedbackComponent
    }

    fun inject(feedbackFragment: FeedbackFragment)
}