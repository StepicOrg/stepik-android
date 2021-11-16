package org.stepik.android.domain.course.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.toObservable
import org.solovyev.android.checkout.ProductTypes
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.model.CoursePurchaseFlow
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.model.SourceTypeComposition
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.mobile_tiers.interactor.MobileTiersInteractor
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.wishlist.repository.WishlistRepository
import org.stepik.android.model.Course
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Progress
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import ru.nobird.android.core.model.mapToLongArray
import javax.inject.Inject

class CourseStatsInteractor
@Inject
constructor(
    private val courseReviewRepository: CourseReviewSummaryRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val progressRepository: ProgressRepository,
    private val profileRepository: ProfileRepository,
    private val wishlistRepository: WishlistRepository,
    private val mobileTiersRepository: MobileTiersRepository,
    private val lightSkuRepository: LightSkuRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val mobileTiersInteractor: MobileTiersInteractor,
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {

    fun getCourseStats(
        courses: List<Course>,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE,
        resolveEnrollmentState: Boolean = true
    ): Single<List<CourseStats>> =
        zip(
            resolveCourseReview(courses, sourceTypeComposition.generalSourceType),
            resolveCourseProgress(courses, sourceTypeComposition.generalSourceType),
            resolveCoursesEnrollmentStates(courses, sourceTypeComposition.enrollmentSourceType, resolveEnrollmentState),
            resolveWishlistStates(sourceTypeComposition.generalSourceType)
        ) { courseReviews, courseProgresses, enrollmentStates, wishlistStates ->
            val reviewsMap = courseReviews.associateBy(CourseReviewSummary::course)
            val progressMaps = courseProgresses.associateBy(Progress::id)
            val enrollmentMap = enrollmentStates.toMap()

            courses.map { course ->
                CourseStats(
                    review = reviewsMap[course.id]?.average ?: 0.0,
                    learnersCount = course.learnersCount,
                    readiness = course.readiness,
                    progress = course.progress?.let(progressMaps::get),
                    enrollmentState = enrollmentMap.getValue(course.id),
                    isWishlisted = wishlistStates.contains(course.id)
                )
            }
        }

    fun getCourseStatsMobileTiersSingle(
        course: Course,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE
    ): Single<CourseStats> =
        zip(
            resolveCourseReview(listOf(course), sourceTypeComposition.generalSourceType),
            resolveCourseProgress(listOf(course), sourceTypeComposition.generalSourceType),
            resolveEnrollmentStateMobileTiersSingleCourse(course, sourceTypeComposition.generalSourceType),
            resolveWishlistStates(sourceTypeComposition.generalSourceType)
        ) { courseReviews, courseProgresses, enrollmentState, wishlistStates ->
            val reviewsMap = courseReviews.associateBy(CourseReviewSummary::course)
            val progressMaps = courseProgresses.associateBy(Progress::id)

            CourseStats(
                review = reviewsMap[course.id]?.average ?: 0.0,
                learnersCount = course.learnersCount,
                readiness = course.readiness,
                progress = course.progress?.let(progressMaps::get),
                enrollmentState = enrollmentState,
                isWishlisted = wishlistStates.contains(course.id)
            )
        }

    fun getCourseStatsMobileTiers(
        courses: List<Course>,
        mobileTiers: List<MobileTier>,
        lightSkus: List<LightSku>,
        sourceTypeComposition: SourceTypeComposition = SourceTypeComposition.REMOTE
    ): Single<List<CourseStats>> =
        zip(
            resolveCourseReview(courses, sourceTypeComposition.generalSourceType),
            resolveCourseProgress(courses, sourceTypeComposition.generalSourceType),
            profileRepository.getProfile(),
            resolveWishlistStates(sourceTypeComposition.generalSourceType)
        ) { courseReviews, courseProgresses, profile, wishlistStates ->
            val reviewsMap = courseReviews.associateBy(CourseReviewSummary::course)
            val progressMaps = courseProgresses.associateBy(Progress::id)

            val mobileTiersByCourseId = mobileTiers.associateBy(MobileTier::course)
            val lightSkusByTierId = lightSkus.associateBy(LightSku::id)
            val enrollmentMap =
                courses
                    .map {
                        val currentMobileTier = mobileTiersByCourseId[it.id]
                        val standardLightSku = currentMobileTier?.priceTier?.let { tier -> lightSkusByTierId[tier] }
                        val promoLightSku = currentMobileTier?.promoTier?.let { tier -> lightSkusByTierId[tier] }
                        resolveEnrollmentStateMobileTiers(it, profile.id, standardLightSku, promoLightSku)
                    }
                    .toMap()

            courses.map { course ->
                CourseStats(
                    review = reviewsMap[course.id]?.average ?: 0.0,
                    learnersCount = course.learnersCount,
                    readiness = course.readiness,
                    progress = course.progress?.let(progressMaps::get),
                    enrollmentState = enrollmentMap.getValue(course.id),
                    isWishlisted = wishlistStates.contains(course.id)
                )
            }
        }

    fun checkDeeplinkPromoCodeValidityMobileTiers(courseId: Long, promo: String): Single<Pair<DeeplinkPromoCode, PromoCodeSku>> =
        coursePaymentsRepository
            .checkDeeplinkPromoCodeValidity(courseId, promo)
            .flatMap { deeplinkPromoCode ->
                if (firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID].asString() == CoursePurchaseFlow.PURCHASE_FLOW_IAP || RemoteConfig.PURCHASE_FLOW_ANDROID_TESTING_FLAG) {
                    mobileTiersRepository
                        .calculateMobileTier(MobileTierCalculation(course = courseId, promo = promo), dataSourceType = DataSourceType.REMOTE)
                        .flatMapSingle { mobileTier ->
                            if (mobileTier.promoTier == null) {
                                Single.just(deeplinkPromoCode to PromoCodeSku.EMPTY)
                            } else {
                                lightSkuRepository
                                    .getLightInventory(ProductTypes.IN_APP, listOf(mobileTier.promoTier), dataSourceType = DataSourceType.REMOTE)
                                    .map { lightSku -> deeplinkPromoCode to PromoCodeSku(promo, lightSku.firstOrNull()) }
                            }
                        }
                } else {
                    Single.just(deeplinkPromoCode to PromoCodeSku.EMPTY)
                }
            }
            .onErrorReturnItem(DeeplinkPromoCode.EMPTY to PromoCodeSku.EMPTY)

    /**
     * Load course reviews for not enrolled [courses]
     */
    private fun resolveCourseReview(courses: List<Course>, sourceType: DataSourceType): Single<List<CourseReviewSummary>> =
        courseReviewRepository
            .getCourseReviewSummaries(courseReviewSummaryIds = courses.filter { it.enrollment == 0L }.mapToLongArray { it.reviewSummary }, sourceType = sourceType)
            .onErrorReturnItem(emptyList())

    private fun resolveCourseProgress(courses: List<Course>, sourceType: DataSourceType): Single<List<Progress>> =
        progressRepository
            .getProgresses(progressIds = courses.getProgresses(), primarySourceType = sourceType)

    private fun resolveCoursesEnrollmentStates(courses: List<Course>, sourceType: DataSourceType, resolveEnrollmentState: Boolean): Single<List<Pair<Long, EnrollmentState>>> =
        courses
            .toObservable()
            .flatMapSingle { resolveCourseEnrollmentState(it, sourceType, resolveEnrollmentState) }
            .toList()

    private fun resolveWishlistStates(sourceType: DataSourceType): Single<Set<Long>> =
        if (sharedPreferenceHelper.authResponseFromStore != null) {
            wishlistRepository
                .getWishlistRecord(sourceType)
                .map { it.courses.toSet() }
                .onErrorReturnItem(emptySet())
        } else {
            Single.just(emptySet())
        }

    private fun resolveCourseEnrollmentState(course: Course, sourceType: DataSourceType, resolveEnrollmentState: Boolean): Single<Pair<Long, EnrollmentState>> =
        when {
            course.enrollment > 0 ->
                profileRepository.getProfile().map { profile -> course.id to resolveEnrolledEnrollmentState(course, profile.id) }

            !course.isPaid ->
                Single.just(course.id to EnrollmentState.NotEnrolledFree)

            resolveEnrollmentState ->
                coursePaymentsRepository
                    .getCoursePaymentsByCourseId(course.id, coursePaymentStatus = CoursePayment.Status.SUCCESS, sourceType = sourceType)
                    .flatMap { payments ->
                        if (payments.isEmpty()) {
                            Single.just(course.id to EnrollmentState.NotEnrolledWeb)
                        } else {
                            Single.just(course.id to EnrollmentState.NotEnrolledFree)
                        }
                    }
                    .onErrorReturnItem(course.id to EnrollmentState.NotEnrolledWeb) // if billing not supported on current device or to access paid course offline

            else ->
                Single.just(course.id to EnrollmentState.NotEnrolledFree)
        }

    private fun resolveEnrollmentStateMobileTiers(course: Course, profileId: Long, standardLightSku: LightSku?, promoLightSku: LightSku?): Pair<Long, EnrollmentState> =
        when {
            course.enrollment > 0 ->
                course.id to resolveEnrolledEnrollmentState(course, profileId)

            !course.isPaid ->
                course.id to EnrollmentState.NotEnrolledFree

            course.isPaid -> {
                if (standardLightSku == null) {
                    course.id to EnrollmentState.NotEnrolledWeb
                } else {
                    course.id to EnrollmentState.NotEnrolledMobileTier(standardLightSku, promoLightSku)
                }
            }

            else ->
                course.id to EnrollmentState.NotEnrolledFree
        }

    private fun resolveEnrollmentStateMobileTiersSingleCourse(course: Course, sourceType: DataSourceType): Single<EnrollmentState> =
        when {
            course.enrollment > 0 ->
                profileRepository.getProfile().map { profile -> resolveEnrolledEnrollmentState(course, profile.id) }

            !course.isPaid ->
                Single.just(EnrollmentState.NotEnrolledFree)

            course.isPaid ->
                mobileTiersInteractor
                    .fetchTiersAndSkus(listOf(course), sourceType = sourceType)
                    .flatMap { (mobileTiers, lightSkus) ->
                        val mobileTiersByCourseId = mobileTiers.associateBy(MobileTier::course)
                        val lightSkusByTierId = lightSkus.associateBy(LightSku::id)
                        val (standardLightSku, promoLightSku) = course.let {
                            val currentMobileTier = mobileTiersByCourseId[it.id]
                            currentMobileTier?.priceTier?.let { tier -> lightSkusByTierId[tier] } to currentMobileTier?.promoTier?.let { tier -> lightSkusByTierId[tier] }
                        }
                        coursePaymentsRepository
                            .getCoursePaymentsByCourseId(course.id, coursePaymentStatus = CoursePayment.Status.SUCCESS, sourceType = sourceType)
                            .flatMap { payments ->
                                if (payments.isEmpty()) {
                                    Single.just(resolvePaidEnrollmentState(standardLightSku, promoLightSku))
                                } else {
                                    Single.just(EnrollmentState.NotEnrolledFree)
                                }
                            }
                            .onErrorReturnItem(resolvePaidEnrollmentState(standardLightSku, promoLightSku))
                    }

            else ->
                Single.just(EnrollmentState.NotEnrolledFree)
        }

    private fun resolveEnrolledEnrollmentState(course: Course, profileId: Long): EnrollmentState.Enrolled =
        EnrollmentState.Enrolled(
            UserCourse(
                course = course.id,
                user = profileId,
                isArchived = course.isArchived,
                isFavorite = course.isFavorite,
                lastViewed = null
            )
        )

    private fun resolvePaidEnrollmentState(standardLightSku: LightSku?, promoLightSku: LightSku?): EnrollmentState =
        if (standardLightSku == null) {
            EnrollmentState.NotEnrolledWeb
        } else {
            EnrollmentState.NotEnrolledMobileTier(standardLightSku, promoLightSku)
        }
}