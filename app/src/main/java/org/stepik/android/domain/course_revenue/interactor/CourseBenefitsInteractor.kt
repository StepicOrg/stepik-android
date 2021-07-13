package org.stepik.android.domain.course_revenue.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.domain.course_revenue.repository.CourseBenefitByMonthsRepository
import org.stepik.android.domain.course_revenue.repository.CourseBenefitSummariesRepository
import org.stepik.android.domain.course_revenue.repository.CourseBenefitsRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class CourseBenefitsInteractor
@Inject
constructor(
    private val courseBenefitSummariesRepository: CourseBenefitSummariesRepository,
    private val courseBenefitsRepository: CourseBenefitsRepository,
    private val courseBenefitsByMonthsRepository: CourseBenefitByMonthsRepository,
    private val userRepository: UserRepository
) {
    fun getCourseBenefitSummary(courseId: Long): Maybe<CourseBenefitSummary> =
        courseBenefitSummariesRepository
            .getCourseBenefitSummaries(courseId)
            .maybeFirst()

    fun getCourseBenefits(courseId: Long): Maybe<List<CourseBenefitListItem.Data>> =
        courseBenefitsRepository
            .getCourseBenefits(courseId)
            .flatMap { resolveCourseBenefitListItems(it) }

    fun getCourseBenefitsByMonths(courseId: Long): Single<List<CourseBenefitByMonthListItem.Data>> =
        courseBenefitsByMonthsRepository
            .getCourseBenefitByMonths(courseId)
            .map { courseBenefitsByMonths ->
                courseBenefitsByMonths.map { CourseBenefitByMonthListItem.Data(it) }
            }

    private fun resolveCourseBenefitListItems(courseBenefits: List<CourseBenefit>): Maybe<List<CourseBenefitListItem.Data>> =
        userRepository
            .getUsers(courseBenefits.map(CourseBenefit::buyer))
            .map { users ->
                val userMap = users.associateBy(User::id)
                courseBenefits.map { CourseBenefitListItem.Data(it, userMap[it.buyer]) }
            }
            .toMaybe()
}