package org.stepik.android.view.course_revenue.model

import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import ru.nobird.android.core.model.Identifiable

sealed class CourseBenefitOperationItem {
    class PurchasesAndRefunds(val state: CourseBenefitsFeature.State) : CourseBenefitOperationItem(), Identifiable<String> {
        override val id: String =
            "purchases_and_refunds"
    }
}