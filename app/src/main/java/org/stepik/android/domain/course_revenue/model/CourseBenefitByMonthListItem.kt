package org.stepik.android.domain.course_revenue.model

sealed class CourseBenefitByMonthListItem {
    data class Data(val courseBenefitByMonth: CourseBenefitByMonth) : CourseBenefitByMonthListItem()
    object Placeholder : CourseBenefitByMonthListItem()
}