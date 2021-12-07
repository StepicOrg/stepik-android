package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes.zip
import io.reactivex.subjects.PublishSubject
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.billing.exception.NoPurchasesToRestoreException
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course.model.CoursePurchasePayload
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.exception.CourseAlreadyOwnedException
import org.stepik.android.domain.course_payments.exception.CoursePurchaseVerificationException
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.interactor.UserCoursesInteractor
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import ru.nobird.android.domain.rx.maybeFirst
import javax.inject.Inject

class CoursePurchaseInteractor
@Inject
constructor(
    private val userCoursesInteractor: UserCoursesInteractor,

    private val billingRepository: BillingRepository,
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val profileRepository: ProfileRepository,

    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>
) {

    fun checkPromoCodeValidity(courseId: Long, promoCodeName: String): Single<PromoCodeSku> =
        mobileTiersRepository
            .calculateMobileTier(MobileTierCalculation(course = courseId, promo = promoCodeName), dataSourceType = DataSourceType.REMOTE)
            .flatMapSingle { mobileTier ->
                if (mobileTier.promoTier == null) {
                    Single.just(PromoCodeSku.EMPTY)
                } else {
                    lightSkuRepository
                        .getLightInventory(BillingClient.SkuType.INAPP, listOf(mobileTier.promoTier), dataSourceType = DataSourceType.REMOTE)
                        .map { lightSku -> PromoCodeSku(promoCodeName, lightSku.firstOrNull()) }
                }
            }

    fun fetchPurchaseFlowData(courseId: Long, skuId: String): Single<Pair<CoursePurchaseObfuscatedParams, SkuDetails>> =
        coursePaymentsRepository
            .getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS, sourceType = DataSourceType.REMOTE)
            .flatMap { payments ->
                if (payments.isEmpty()) {
                    getSkuDetails(courseId, skuId)
                } else {
                    Single.error(CourseAlreadyOwnedException(courseId))
                }
            }

    fun completePurchase(courseId: Long, sku: SkuDetails, purchase: Purchase): Completable =
        coursePaymentsRepository
            .createCoursePayment(courseId, sku, purchase)
            .flatMapCompletable { payment ->
                if (payment.status == CoursePayment.Status.SUCCESS) {
                    Completable.complete()
                } else {
                    Completable.error(CoursePurchaseVerificationException())
                }
            }
            .andThen(billingRepository.consumePurchase(purchase))
            .andThen(updateCourseAfterEnrollment(courseId))

    fun restorePurchase(courseId: Long, skuId: String): Completable =
        billingRepository
            .getInventory(BillingClient.SkuType.INAPP, skuId)
            .flatMapCompletable { skuDetails ->  restorePurchase(courseId, skuDetails) }

    private fun restorePurchase(courseId: Long, sku: SkuDetails): Completable =
        zip(
            getCurrentProfileId()
                .toMaybe(),
            billingRepository
                .getAllPurchases(BillingClient.SkuType.INAPP, listOf(sku.sku))
                .maybeFirst()
        )
            .filter { (profileId, purchase) ->
                val obfuscatedParams = createCoursePurchaseObfuscatedParams(profileId, courseId)
                purchase.accountIdentifiers?.obfuscatedAccountId == obfuscatedParams.obfuscatedAccountId && purchase?.accountIdentifiers?.obfuscatedProfileId == obfuscatedParams.obfuscatedProfileId
            }
            .switchIfEmpty(Single.error(NoPurchasesToRestoreException()))
            .flatMapCompletable { (_, purchase) ->
                completePurchase(courseId, sku, purchase)
            }

    private fun getSkuDetails(courseId: Long, skuId: String): Single<Pair<CoursePurchaseObfuscatedParams, SkuDetails>> =
        getCurrentProfileId()
            .flatMap { profileId ->
                billingRepository
                    .getInventory(BillingClient.SkuType.INAPP, skuId)
                    .toSingle()
                    .map { skuDetails -> createCoursePurchaseObfuscatedParams(profileId, courseId) to skuDetails }
            }

    private fun updateCourseAfterEnrollment(courseId: Long): Completable =
        userCoursesInteractor.addUserCourse(courseId)
            .andThen(lessonRepository.removeCachedLessons(courseId))
            .andThen(courseRepository.getCourse(courseId, sourceType = DataSourceType.REMOTE, allowFallback = false).toSingle())
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes
            .ignoreElement()

    private fun getCurrentProfileId(): Single<Long> =
        profileRepository
            .getProfile()
            .map { profile -> profile.id }

    private fun createCoursePurchaseObfuscatedParams(profileId: Long, courseId: Long): CoursePurchaseObfuscatedParams =
        CoursePurchaseObfuscatedParams(
            obfuscatedAccountId = profileId.toString().hashCode().toString(),
            obfuscatedProfileId = CoursePurchasePayload(profileId, courseId).hashCode().toString()
        )
}