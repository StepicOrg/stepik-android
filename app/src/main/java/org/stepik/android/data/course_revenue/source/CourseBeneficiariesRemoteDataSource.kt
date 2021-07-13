package org.stepik.android.data.course_revenue.source

import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary

interface CourseBeneficiariesRemoteDataSource {
    fun getCourseBeneficiary(courseId: Long, userId: Long): Single<CourseBeneficiary>
}