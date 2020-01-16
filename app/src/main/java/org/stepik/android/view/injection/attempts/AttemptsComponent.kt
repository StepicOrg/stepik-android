package org.stepik.android.view.injection.attempts

import dagger.Subcomponent
import org.stepik.android.view.attempts.ui.activity.AttemptsActivity
import org.stepik.android.view.injection.attempt.AttemptDataModule

@Subcomponent(modules = [AttemptsModule::class, AttemptDataModule::class])
interface AttemptsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AttemptsComponent
    }

    fun inject(attemptsActivity: AttemptsActivity)
}