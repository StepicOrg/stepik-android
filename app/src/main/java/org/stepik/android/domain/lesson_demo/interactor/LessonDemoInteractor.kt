package org.stepik.android.domain.lesson_demo.interactor

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.model.CoursePurchaseFlow
import org.stepik.android.domain.lesson_demo.model.LessonDemoData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class LessonDemoInteractor
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val coursePurchaseDataRepository: CoursePurchaseDataRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {
    fun getLessonDemoData(course: Course): Single<LessonDemoData> {
        val currentFlow = CoursePurchaseFlow.valueOfWithFallback(
            firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID]
                .asString()
                .uppercase()
        )

        val isInAppActive =
            currentFlow.isInAppActive() || RemoteConfig.PURCHASE_FLOW_ANDROID_TESTING_FLAG

        return if (isInAppActive) {
            val deeplinkPromoCode = requireNotNull(coursePurchaseDataRepository.getDeeplinkPromoCode().value)
            val initialCoursePurchaseDataResult = requireNotNull(coursePurchaseDataRepository.getCoursePurchaseData().value)

            if (initialCoursePurchaseDataResult is CoursePurchaseDataResult.Empty) {
                coursePurchaseDataFallback(course, deeplinkPromoCode)
                    .map { coursePurchaseDataFallbackResult ->
                        LessonDemoData(deeplinkPromoCode, (coursePurchaseDataFallbackResult as? CoursePurchaseDataResult.Result)?.coursePurchaseData)
                    }
            } else {
                Single.just(LessonDemoData(deeplinkPromoCode, (initialCoursePurchaseDataResult as CoursePurchaseDataResult.Result).coursePurchaseData))
            }
        } else {
            val deeplinkPromoCode = requireNotNull(coursePurchaseDataRepository.getDeeplinkPromoCode().value)
            Single.just(LessonDemoData(deeplinkPromoCode, null))
        }
    }

    private fun coursePurchaseDataFallback(
        course: Course,
        deeplinkPromoCode: DeeplinkPromoCode
    ): Single<CoursePurchaseDataResult> =
        zip(
            courseStatsInteractor.getCourseStatsMobileTiers(listOf(course)).first(),
            if (deeplinkPromoCode == DeeplinkPromoCode.EMPTY) {
                Single.just(DeeplinkPromoCode.EMPTY to PromoCodeSku.EMPTY)
            } else {
                courseStatsInteractor.checkDeeplinkPromoCodeValidity(course.id, deeplinkPromoCode.name)
            }
        ) { courseStats, (_, deeplinkPromoCodeSku) ->
            val notEnrolledMobileTierState = (courseStats.enrollmentState as? EnrollmentState.NotEnrolledMobileTier)
            if (notEnrolledMobileTierState != null) {
                val promoCodeSku = when {
                    deeplinkPromoCodeSku != PromoCodeSku.EMPTY ->
                        deeplinkPromoCodeSku

                    notEnrolledMobileTierState.promoLightSku != null -> {
                        PromoCodeSku(
                            course.defaultPromoCodeName.orEmpty(),
                            notEnrolledMobileTierState.promoLightSku
                        )
                    }

                    else ->
                        PromoCodeSku.EMPTY
                }

                CoursePurchaseDataResult.Result(
                    CoursePurchaseData(
                        course,
                        courseStats,
                        notEnrolledMobileTierState.standardLightSku,
                        promoCodeSku,
                        course.isInWishlist
                    )
                )
            } else {
                CoursePurchaseDataResult.Empty
            }
        }
}