package org.stepik.android.domain.course.interactor

import com.google.gson.Gson
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.course.EnrollmentCourseUpdates
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseBillingInteractor
@Inject
constructor(
//    private val billingRepository: BillingRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,

    private val sharedPreferenceHelper: SharedPreferenceHelper,

    private val courseRepository: CourseRepository,
    // private val joiningPoster: JoiningPoster,

    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) {
    private val gson = Gson()

    companion object {
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
    }

//    fun purchaseCourse(checkout: UiCheckout, courseId: Long, sku: Sku): Completable =
//        coursePaymentsRepository
//            .getCoursePaymentsByCourseId(courseId, CoursePayment.Status.SUCCESS)
//            .flatMapCompletable { payments ->
//                if (payments.isEmpty()) {
//                    purchaseCourseAfterCheck(checkout, courseId, sku)
//                } else {
//                    Completable.error(CourseAlreadyOwnedException(courseId))
//                }
//            }

//    private fun purchaseCourseAfterCheck(checkout: UiCheckout, courseId: Long, sku: Sku): Completable =
//        getCurrentProfileId()
//            .map { profileId ->
//                gson.toJson(CoursePurchasePayload(profileId, courseId))
//            }
//            .observeOn(mainScheduler)
//            .flatMap { payload ->
//                checkout.startPurchaseFlowRx(sku, payload)
//            }
//            .observeOn(backgroundScheduler)
//            .flatMapCompletable { purchase ->
//                completePurchase(courseId, sku, purchase)
//            }

//    fun restorePurchase(sku: Sku): Completable =
//        zip(
//            getCurrentProfileId()
//                .toMaybe(),
//            billingRepository
//                .getAllPurchases(ProductTypes.IN_APP, listOf(sku.id.code))
//                .observeOn(backgroundScheduler)
//                .maybeFirst()
//        )
//            .map { (profileId, purchase) ->
//                Triple(profileId, purchase, purchase.payload.toObject<CoursePurchasePayload>(gson))
//            }
//            .filter { (profileId, _, payload) ->
//                profileId == payload.profileId
//            }
//            .switchIfEmpty(Single.error(NoPurchasesToRestoreException()))
//            .flatMapCompletable { (_, purchase, payload) ->
//                completePurchase(payload.courseId, sku, purchase)
//            }

//    private fun completePurchase(courseId: Long, sku: Sku, purchase: Purchase): Completable =
//        coursePaymentsRepository
//            .createCoursePayment(courseId, sku, purchase)
//            .flatMapCompletable { payment ->
//                if (payment.status == CoursePayment.Status.SUCCESS) {
//                    Completable.complete()
//                } else {
//                    Completable.error(CoursePurchaseVerificationException())
//                }
//            }
//            .andThen(updateCourseAfterEnrollment(courseId))
//            .andThen(billingRepository.consumePurchase(purchase))

//    private fun updateCourseAfterEnrollment(courseId: Long): Completable =
//        courseListRepository
//            .addCourseToList(CourseListType.ENROLLED, courseId)
//            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
//            .doOnSuccess(joiningPoster::joinCourse) // interop with old code
//            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes
//            .ignoreElement()

//    private fun getCurrentProfileId(): Single<Long> =
//        Single.fromCallable {
//            sharedPreferenceHelper
//                .profile
//                ?.id
//                ?: throw UNAUTHORIZED_EXCEPTION_STUB
//        }
}