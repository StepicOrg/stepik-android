package org.stepik.android.domain.course_purchase.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Completable
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
import org.stepik.android.domain.course_purchase.model.PurchaseFlowData
import org.stepik.android.domain.course_purchase.repository.BillingPurchasePayloadRepository
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user_courses.interactor.UserCoursesInteractor
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import ru.nobird.android.domain.rx.toMaybe
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

    fun fetchPurchaseFlowData(courseId: Long, skuId: String): Single<PurchaseFlowData> =
        coursePaymentsRepository
            .getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS, sourceType = DataSourceType.REMOTE)
            .flatMap { payments ->
                if (payments.isEmpty()) {
                    getSkuDetails(courseId, skuId)
                } else {
                    Single.error(CourseAlreadyOwnedException(courseId))
                }
            }

    fun consumePurchase(
        courseId: Long,
        profileId: Long,
        sku: SkuDetails,
        purchase: Purchase,
        promoCodeName: String? = null
    ): Completable =
        saveBillingPurchasePayload(courseId, profileId, purchase, promoCodeName)
            .andThen(completePurchase(courseId, sku, purchase, promoCodeName))

    fun restorePurchase(courseId: Long): Completable =
        Singles.zip(
            getCurrentProfileId(),
            billingRepository.getAllPurchases(BillingClient.SkuType.INAPP)
        ).flatMapMaybe { (profileId, purchases) ->
            val obfuscatedParams = createCoursePurchaseObfuscatedParams(profileId, courseId)
            val purchase =
                purchases.find {
                    it.accountIdentifiers?.obfuscatedAccountId == obfuscatedParams.obfuscatedAccountId &&
                        it.accountIdentifiers?.obfuscatedProfileId == obfuscatedParams.obfuscatedProfileId
                }
            purchase.toMaybe()
        }
        .switchIfEmpty(Single.error(NoPurchasesToRestoreException()))
        .flatMapCompletable { purchase ->
            completePurchaseRestore(courseId, purchase)
        }

    fun saveBillingPurchasePayload(courseId: Long, profileId: Long, purchase: Purchase, promoCodeName: String?): Completable =
        billingPurchasePayloadRepository
            .saveBillingPurchasePayload(
                BillingPurchasePayload(
                    orderId = purchase.orderId,
                    courseId = courseId,
                    profileId = profileId,
                    obfuscatedAccountId = purchase.accountIdentifiers?.obfuscatedAccountId.orEmpty(),
                    obfuscatedProfileId = purchase.accountIdentifiers?.obfuscatedProfileId.orEmpty(),
                    promoCode = promoCodeName
                )
            )

    private fun completePurchase(courseId: Long, sku: SkuDetails, purchase: Purchase, promoCodeName: String? = null): Completable =
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

    private fun completePurchaseRestore(courseId: Long, purchase: Purchase): Completable =
        Singles.zip(
            billingPurchasePayloadRepository.getBillingPurchasePayload(purchase.orderId).onErrorReturnItem(BillingPurchasePayload.EMPTY),
            billingRepository.getInventory(BillingClient.SkuType.INAPP, purchase.skus.first()).toSingle()
        ).flatMapCompletable { (billingPurchasePayload, skuDetails) ->
            val promoCode =
                if (billingPurchasePayload.obfuscatedAccountId == purchase.accountIdentifiers?.obfuscatedAccountId &&
                    billingPurchasePayload.obfuscatedProfileId == purchase.accountIdentifiers?.obfuscatedProfileId
                ) {
                    billingPurchasePayload.promoCode
                } else {
                    null
                }
            completePurchase(courseId, skuDetails, purchase, promoCode)
        }

    private fun getSkuDetails(courseId: Long, skuId: String): Single<PurchaseFlowData> =
        getCurrentProfileId()
            .flatMap { profileId ->
                billingRepository
                    .getInventory(BillingClient.SkuType.INAPP, skuId)
                    .toSingle()
                    .map { skuDetails ->
                        PurchaseFlowData(
                            CoursePurchasePayload(profileId, courseId),
                            createCoursePurchaseObfuscatedParams(profileId, courseId),
                            skuDetails
                        )
                    }
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