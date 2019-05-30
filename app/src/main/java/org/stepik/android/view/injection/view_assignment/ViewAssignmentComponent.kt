package org.stepik.android.view.injection.view_assignment

import dagger.Subcomponent
import org.stepik.android.domain.view_assignment.service.DeferrableViewAssignmentReportService

@Subcomponent(modules = [
    ViewAssignmentDataModule::class
])
interface ViewAssignmentComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ViewAssignmentComponent
    }

    fun inject(deferrableViewAssignmentReportService: DeferrableViewAssignmentReportService)
}