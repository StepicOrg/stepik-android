package org.stepik.android.domain.course_revenue.repository

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary

interface CourseBeneficiariesRepository {
    fun getCourseBeneficiaries(courseId: Long): Single<CourseBeneficiary>
}