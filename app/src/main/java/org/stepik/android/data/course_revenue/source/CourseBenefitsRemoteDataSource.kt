package org.stepik.android.data.course_revenue.source

import io.reactivex.Maybe
import org.stepik.android.domain.course_revenue.model.CourseBenefit

interface CourseBenefitsRemoteDataSource {
    fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefit>>
}