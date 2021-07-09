package org.stepik.android.domain.course_benefits.model

sealed class CourseBenefitListItem {
    data class Data(val courseBenefit: CourseBenefit) : CourseBenefitListItem()
    object Placeholder : CourseBenefitListItem()
}