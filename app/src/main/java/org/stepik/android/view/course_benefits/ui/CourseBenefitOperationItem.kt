package org.stepik.android.view.course_benefits.ui

import org.stepik.android.presentation.course_benefits.CourseBenefitsPurchasesAndRefundsFeature
import ru.nobird.android.core.model.Identifiable

sealed class CourseBenefitOperationItem {
    class PurchasesAndRefunds(val state: CourseBenefitsPurchasesAndRefundsFeature.State) : CourseBenefitOperationItem(), Identifiable<String> {
        override val id: String =
            "purchases_and_refunds"
    }
}