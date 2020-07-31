package org.stepik.android.view.injection.analytic

import dagger.Subcomponent
import org.stepik.android.view.analytic.AnalyticContentProvider

@Subcomponent(modules = [AnalyticModule::class])
interface AnalyticComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AnalyticComponent
    }

    fun inject(analyticContentProvider: AnalyticContentProvider)
}