package org.stepik.android.domain.course_benefits.interactor

import io.reactivex.Maybe
import org.stepik.android.domain.course_benefits.model.CourseBenefit
import org.stepik.android.domain.course_benefits.model.CourseBenefitSummary
import org.stepik.android.domain.course_benefits.repository.CourseBenefitSummariesRepository
import org.stepik.android.domain.course_benefits.repository.CourseBenefitsRepository
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class CourseBenefitsInteractor
@Inject
constructor(
    private val courseBenefitSummariesRepository: CourseBenefitSummariesRepository,
    private val courseBenefitsRepository: CourseBenefitsRepository
) {
    fun getCourseBenefitSummary(courseId: Long): Maybe<CourseBenefitSummary> =
        courseBenefitSummariesRepository
            .getCourseBenefitSummaries(courseId)
            .maybeFirst()

    fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefit>> =
        courseBenefitsRepository
            .getCourseBenefits(courseId)
}