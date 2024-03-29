package org.stepik.android.domain.course_revenue.interactor

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.domain.course_revenue.model.CourseBenefitByMonthListItem
import org.stepik.android.domain.course_revenue.model.CourseBenefitListItem
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.domain.course_revenue.repository.CourseBenefitByMonthsRepository
import org.stepik.android.domain.course_revenue.repository.CourseBeneficiariesRepository
import org.stepik.android.domain.course_revenue.repository.CourseBenefitSummariesRepository
import org.stepik.android.domain.course_revenue.repository.CourseBenefitsRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.user.User
import ru.nobird.app.core.model.PagedList
import ru.nobird.app.core.model.transform
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class CourseBenefitsInteractor
@Inject
constructor(
    private val courseBenefitSummariesRepository: CourseBenefitSummariesRepository,
    private val courseBenefitsRepository: CourseBenefitsRepository,
    private val courseBenefitsByMonthsRepository: CourseBenefitByMonthsRepository,
    private val userRepository: UserRepository,
    private val courseBeneficiariesRepository: CourseBeneficiariesRepository,
    private val profileRepository: ProfileRepository
) {
    fun getCourseBenefitSummary(courseId: Long): Maybe<CourseBenefitSummary> =
        courseBenefitSummariesRepository
            .getCourseBenefitSummaries(courseId)
            .maybeFirst()

    fun getCourseBenefits(courseId: Long, page: Int = 1): Single<PagedList<CourseBenefitListItem.Data>> =
        courseBenefitsRepository
            .getCourseBenefits(courseId, page)
            .flatMap { resolveCourseBenefitListItems(it) }

    fun getCourseBeneficiary(courseId: Long): Single<CourseBeneficiary> =
        profileRepository
            .getProfile()
            .flatMap { profile ->
                courseBeneficiariesRepository
                    .getCourseBeneficiary(courseId, profile.id)
            }

    fun getCourseBenefitsByMonths(courseId: Long, page: Int): Single<PagedList<CourseBenefitByMonthListItem.Data>> =
        courseBenefitsByMonthsRepository
            .getCourseBenefitByMonths(courseId, page)
            .map { courseBenefitsByMonths ->
                courseBenefitsByMonths.transform {
                    map { CourseBenefitByMonthListItem.Data(it) }
                }
            }

    private fun resolveCourseBenefitListItems(courseBenefits: PagedList<CourseBenefit>): Single<PagedList<CourseBenefitListItem.Data>> =
        userRepository
            .getUsers(courseBenefits.mapNotNull(CourseBenefit::buyer))
            .map { users ->
                val userMap = users.associateBy(User::id)
                courseBenefits.transform {
                    map { CourseBenefitListItem.Data(it, userMap[it.buyer]) }
                }
            }
}