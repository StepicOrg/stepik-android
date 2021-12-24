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
import org.stepik.android.presentation.course.resolver.CoursePurchaseDataResolver
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import ru.nobird.android.domain.rx.first
import javax.inject.Inject

class LessonDemoInteractor
@Inject
constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val coursePurchaseDataRepository: CoursePurchaseDataRepository,
    private val courseStatsInteractor: CourseStatsInteractor,
    private val coursePurchaseDataResolver: CoursePurchaseDataResolver
) {
    fun getLessonDemoData(course: Course): Single<LessonDemoData> {
        val currentFlow = CoursePurchaseFlow.valueOfWithFallback(
            firebaseRemoteConfig[RemoteConfig.PURCHASE_FLOW_ANDROID]
                .asString()
                .uppercase()
        )

        val isInAppActive =
            currentFlow.isInAppActive()

        val deeplinkPromoCode = coursePurchaseDataRepository.getDeeplinkPromoCode()
        val initialCoursePurchaseDataResult = coursePurchaseDataRepository.getCoursePurchaseData()

        return if (isInAppActive) {
            if (initialCoursePurchaseDataResult is CoursePurchaseDataResult.Empty) {
                coursePurchaseDataFallback(course, deeplinkPromoCode)
                    .map { coursePurchaseDataFallbackResult ->
                        LessonDemoData(deeplinkPromoCode, coursePurchaseDataFallbackResult)
                    }
            } else {
                Single.just(LessonDemoData(deeplinkPromoCode, initialCoursePurchaseDataResult))
            }
        } else {
            Single.just(LessonDemoData(coursePurchaseDataRepository.getDeeplinkPromoCode(), CoursePurchaseDataResult.Empty))
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
        ) { stats, (_, deeplinkPromoCodeSku) ->
            when (stats.enrollmentState) {
                is EnrollmentState.NotEnrolledMobileTier ->
                    coursePurchaseDataResolver
                        .resolveCoursePurchaseData(course, stats, stats.enrollmentState, deeplinkPromoCodeSku)
                        .let { CoursePurchaseDataResult.Result(it) }

                is EnrollmentState.NotEnrolledUnavailable ->
                    CoursePurchaseDataResult.NotAvailable

                else ->
                    CoursePurchaseDataResult.Empty
            }
        }
}