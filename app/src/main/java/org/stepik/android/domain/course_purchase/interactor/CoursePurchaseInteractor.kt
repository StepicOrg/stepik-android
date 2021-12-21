package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
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
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams
import org.stepik.android.domain.course_purchase.repository.BillingPurchasePayloadRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.interactor.UserCoursesInteractor
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
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
    private val billingPurchasePayloadRepository: BillingPurchasePayloadRepository,

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

    fun fetchPurchaseFlowData(courseId: Long, skuId: String): Single<SkuDetails> =
        coursePaymentsRepository
            .getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS, sourceType = DataSourceType.REMOTE)
            .flatMap { payments ->
                if (payments.isEmpty()) {
                    getSkuDetails(skuId)
                } else {
                    Single.error(CourseAlreadyOwnedException(courseId))
                }
            }

    fun saveBillingPurchasePayload(purchase: Purchase, promoCodeName: String?): Completable =
        billingPurchasePayloadRepository
            .saveBillingPurchasePayload(
                BillingPurchasePayload(
                    orderId = purchase.orderId,
                    obfuscatedAccountId = purchase.accountIdentifiers?.obfuscatedAccountId!!,
                    obfuscatedProfileId = purchase.accountIdentifiers?.obfuscatedProfileId!!,
                    promoCode = promoCodeName
                )
            )

    fun completePurchase(courseId: Long, sku: SkuDetails, purchase: Purchase, promoCodeName: String? = null): Completable =
        coursePaymentsRepository
            .createCoursePayment(courseId, sku, purchase, promoCodeName)
            .flatMapCompletable { payment ->
                if (payment.status == CoursePayment.Status.SUCCESS) {
                    Completable.complete()
                } else {
                    Completable.error(CoursePurchaseVerificationException())
                }
            }
            .andThen(billingRepository.consumePurchase(purchase))
            .andThen(billingPurchasePayloadRepository.deleteBillingPurchasePayload(purchase.orderId))
            .andThen(updateCourseAfterEnrollment(courseId))
    //        Completable.error(Exception()) // TODO Use this to break purchase, in order to test Restore

    fun restorePurchase(courseId: Long, obfuscatedParams: CoursePurchaseObfuscatedParams): Completable =
        billingRepository
            .getAllPurchases(BillingClient.SkuType.INAPP)
            .flatMapMaybe { purchases ->
                val purchase =
                    purchases.find {
                        it.accountIdentifiers?.obfuscatedAccountId == obfuscatedParams.obfuscatedAccountId &&
                        it.accountIdentifiers?.obfuscatedProfileId == obfuscatedParams.obfuscatedProfileId
                    }
                if (purchase == null) {
                    Maybe.empty()
                } else {
                    Maybe.just(purchase)
                }
            }
            .switchIfEmpty(Single.error(NoPurchasesToRestoreException()))
            .flatMapCompletable { purchase ->
                completePurchaseRestore(courseId, purchase)
            }

    private fun completePurchaseRestore(courseId: Long, purchase: Purchase): Completable =
        Singles.zip(
            billingPurchasePayloadRepository.getBillingPurchasePayload(purchase.orderId),
            billingRepository.getInventory(BillingClient.SkuType.INAPP, purchase.skus.first()).toSingle()
        ).flatMapCompletable { (billingPurchasePayload, skuDetails) ->
            if (billingPurchasePayload.obfuscatedAccountId == purchase.accountIdentifiers?.obfuscatedAccountId &&
                billingPurchasePayload.obfuscatedProfileId == purchase.accountIdentifiers?.obfuscatedProfileId
            ) {
                completePurchase(courseId, skuDetails, purchase, billingPurchasePayload.promoCode)
            } else {
                Completable.error(NoPurchasesToRestoreException())
            }
        }

    private fun getSkuDetails(skuId: String): Single<SkuDetails> =
        billingRepository
            .getInventory(BillingClient.SkuType.INAPP, skuId)
            .toSingle()

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