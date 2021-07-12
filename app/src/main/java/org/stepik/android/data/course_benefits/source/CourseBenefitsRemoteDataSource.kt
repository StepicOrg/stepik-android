package org.stepik.android.data.course_benefits.source

import io.reactivex.Maybe
import org.stepik.android.domain.course_benefits.model.CourseBenefit

interface CourseBenefitsRemoteDataSource {
    fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefit>>
}