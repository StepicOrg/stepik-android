package org.stepik.android.view.course_revenue.model

import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import ru.nobird.app.core.model.Identifiable

sealed class CourseBenefitOperationItem {
    data class CourseBenefits(
        val state: CourseBenefitsFeature.State
    ) : CourseBenefitOperationItem(), Identifiable<String> {
        override val id: String =
            "course_benefits"
    }

    data class CourseBenefitsMonthly(
        val state: CourseBenefitsMonthlyFeature.State
    ) : CourseBenefitOperationItem(), Identifiable<String> {
        override val id: String =
            "course_benefits_monthly"
    }
}