package org.stepic.droid.di.feedback

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.TextFeedbackFragment

@Subcomponent
@FeedbackScope
interface FeedbackComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FeedbackComponent
    }

    fun inject(feedbackFragment: TextFeedbackFragment)
}
