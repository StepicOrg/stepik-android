package org.stepik.android.domain.course.interactor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.subjects.BehaviorSubject
import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.course.model.CoursePurchasePayload
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.mapper.DefaultPromoCodeMapper
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.model.CoursePurchaseFlow
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams
import org.stepik.android.domain.course_purchase.model.PurchaseResult
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.solutions.interactor.SolutionsInteractor
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.resolver.CoursePurchaseDataResolver
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import org.stepik.android.view.injection.course.CourseScope
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

@CourseScope
class CourseInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val solutionsInteractor: SolutionsInteractor,
    private val coursePublishSubject: BehaviorSubject<Course>,
    private val courseStatsInteractor: CourseStatsInteractor,
    private val defaultPromoCodeMapper: DefaultPromoCodeMapper,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val coursePurchaseDataRepository: CoursePurchaseDataRepository,
    private val coursePurchaseDataResolver: CoursePurchaseDataResolver,
    private val profileRepository: ProfileRepository,
    private val billingRepository: BillingRepository
) {

    fun getCourseHeaderData(courseId: Long, promo: String? = null, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(courseId, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap { obtainCourseHeaderData(it, promo) }

    /**
     * Trying to fetch DB data in first place as course object passed with intent could be obsolete
     */
    fun getCourseHeaderData(course: Course, canUseCache: Boolean = true): Maybe<CourseHeaderData> =
        courseRepository
            .getCourse(course.id, if (canUseCache) DataSourceType.CACHE else DataSourceType.REMOTE, canUseCache)
            .onErrorReturnItem(course)
            .doOnSuccess(coursePublishSubject::onNext)
            .flatMap(::obtainCourseHeaderData)

    private fun obtainCourseHeaderData(course: Course, promo: String? = null): Maybe<CourseHeaderData> {
        val currentFlow = CoursePurchaseFlow.valueOfWithFallback(
            firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID]
                .asString()
                .uppercase()
        )

        val isInAppActive =
            currentFlow.isInAppActive() || RemoteConfig.PURCHASE_FLOW_ANDROID_TESTING_FLAG

        return zip(
            if (isInAppActive) {
                courseStatsInteractor.getCourseStatsMobileTiers(listOf(course)).first()
            } else {
                courseStatsInteractor.getCourseStats(listOf(course)).first()
            },
            solutionsInteractor.fetchAttemptCacheItems(course.id, localOnly = true),
            if (promo == null) {
                Single.just(DeeplinkPromoCode.EMPTY to PromoCodeSku.EMPTY)
            } else {
                courseStatsInteractor.checkDeeplinkPromoCodeValidity(course.id, promo)
            },
            if (isInAppActive && course.isPaid) {
                getInitialPurchaseResult(course.id)
            } else {
                Single.just(PurchaseResult.Unavailable)
            }
        ) { courseStats, localSubmissions, (promoCode, promoCodeSku), purchaseResult ->
            CourseHeaderData(
                courseId = course.id,
                course = course,
                title = course.title ?: "",
                cover = course.cover ?: "",

                stats = courseStats,
                localSubmissionsCount = localSubmissions.count { it is SolutionItem.SubmissionItem },
                deeplinkPromoCode = promoCode,
                deeplinkPromoCodeSku = promoCodeSku,
                defaultPromoCode = defaultPromoCodeMapper.mapToDefaultPromoCode(course),
                isWishlistUpdating = false,
                purchaseResult = purchaseResult
            )
        }
            .toMaybe()
            .doOnSuccess { courseHeaderData ->
                val coursePurchaseDataResult =
                    when (courseHeaderData.stats.enrollmentState) {
                        is EnrollmentState.NotEnrolledMobileTier ->
                            coursePurchaseDataResolver
                                .resolveCoursePurchaseData(courseHeaderData)
                                ?.let { CoursePurchaseDataResult.Result(it) }
                                ?: CoursePurchaseDataResult.Empty

                        is EnrollmentState.NotEnrolledUnavailable ->
                            CoursePurchaseDataResult.NotAvailable

                        else ->
                            CoursePurchaseDataResult.Empty
                    }

                coursePurchaseDataRepository.savePurchaseData(courseHeaderData.deeplinkPromoCode, coursePurchaseDataResult)
            }
    }

    fun resolvePurchaseResult(purchaseResult: PurchaseResult, purchases: List<Purchase>): PurchaseResult {
        val obfuscatedParams = when (purchaseResult) {
            is PurchaseResult.Empty ->
                purchaseResult.obfuscatedParams
            is PurchaseResult.Result ->
                purchaseResult.obfuscatedParams
            else ->
                throw IllegalArgumentException()
        }
        return resolvePurchaseResult(obfuscatedParams, purchases)
    }

    private fun getInitialPurchaseResult(courseId: Long): Single<PurchaseResult> =
        zip(
            getCurrentProfileId(),
            billingRepository.getAllPurchases(BillingClient.SkuType.INAPP)
        ) { profileId, purchases ->
            val obfuscatedParams =
                CoursePurchaseObfuscatedParams(
                    obfuscatedAccountId = profileId.toString().hashCode().toString(),
                    obfuscatedProfileId = CoursePurchasePayload(profileId, courseId).hashCode().toString()
                )
            resolvePurchaseResult(obfuscatedParams, purchases)
        }

    private fun resolvePurchaseResult(obfuscatedParams: CoursePurchaseObfuscatedParams, purchases: List<Purchase>): PurchaseResult {
        val purchase =
            purchases.find {
                it.accountIdentifiers?.obfuscatedAccountId == obfuscatedParams.obfuscatedAccountId &&
                    it.accountIdentifiers?.obfuscatedProfileId == obfuscatedParams.obfuscatedProfileId
            }
        return if (purchase == null) {
            PurchaseResult.Empty(obfuscatedParams)
        } else {
            PurchaseResult.Result(obfuscatedParams, purchase)
        }
    }

    private fun getCurrentProfileId(): Single<Long> =
        profileRepository
            .getProfile()
            .map { profile -> profile.id }
}