package org.stepik.android.domain.course_revenue.model

import org.stepik.android.model.user.User

sealed class CourseBenefitListItem {
    data class Data(val courseBenefit: CourseBenefit, val user: User?) : CourseBenefitListItem()
    object Placeholder : CourseBenefitListItem()
}