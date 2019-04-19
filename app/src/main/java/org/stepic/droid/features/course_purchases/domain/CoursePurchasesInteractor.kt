package org.stepic.droid.features.course_purchases.domain

import io.reactivex.Single
import org.solovyev.android.checkout.Sku
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.model.Course
import javax.inject.Inject

/**
 * Interactor to work with legacy presenters. Should be removed after course lists refactoring.
 */
class CoursePurchasesInteractor
@Inject
constructor(
    private val billingRepository: BillingRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository
) {
    companion object {
        private const val COURSE_TIER_PREFIX = "course_tier_"
    }

    fun getCoursesPaymentsMap(courses: List<Course>): Single<Map<Long, CoursePayment>> =
        Single
            .concat(courses.filter(Course::isPaid).map { coursePaymentsRepository.getCoursePaymentsByCourseId(it.id, CoursePayment.Status.SUCCESS) })
            .toList()
            .map { coursePayments ->
                coursePayments
                    .flatten()
                    .associateBy(CoursePayment::course)
            }
            .onErrorReturnItem(emptyMap())

    fun getCoursesSkuMap(courses: List<Course>): Single<Map<String, Sku>> =
        Single.just(emptyMap())
//        courses
//            .mapNotNull { course ->
//                course.priceTier?.let { COURSE_TIER_PREFIX + it }
//            }
//            .let { skuIds ->
//                billingRepository
//                    .getInventory(ProductTypes.IN_APP, skuIds)
//            }
//            .map { skus ->
//                skus.associateBy { it.id.code }
//                    .mapKeys { it.key.removePrefix(COURSE_TIER_PREFIX) }
//            }
//            .onErrorReturnItem(emptyMap())
}