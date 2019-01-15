package org.stepik.android.domain.course.interactor

import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody
import org.solovyev.android.checkout.Sku
import org.solovyev.android.checkout.UiCheckout
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.model.CourseListType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.startPurchaseFlowRx
import org.stepik.android.domain.billing.repository.BillingRepository
import org.stepik.android.domain.course.model.CoursePurchasePayload
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.repository.CourseListRepository
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
    private val billingRepository: BillingRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,

    private val sharedPreferenceHelper: SharedPreferenceHelper,

    private val courseRepository: CourseRepository,
    private val courseListRepository: CourseListRepository,
    private val joiningPoster: JoiningPoster,

    @EnrollmentCourseUpdates
    private val enrollmentSubject: PublishSubject<Course>
) {
    private val gson = Gson()

    companion object {
        private val UNAUTHORIZED_EXCEPTION_STUB =
            HttpException(Response.error<Nothing>(HttpURLConnection.HTTP_UNAUTHORIZED, ResponseBody.create(null, "")))
    }

    fun purchaseCourse(checkout: UiCheckout, courseId: Long, sku: Sku): Single<Course> =
        getCurrentProfileId()
            .flatMap { profileId ->
                checkout
                    .startPurchaseFlowRx(sku, gson.toJson(CoursePurchasePayload(profileId, courseId)))
            }
            .flatMap { purchase ->
                coursePaymentsRepository
                    .createCoursePayment(courseId, sku, purchase)
            }
            .ignoreElement()
            .andThen(courseListRepository.addCourseToList(CourseListType.ENROLLED, courseId))
            .andThen(courseRepository.getCourse(courseId, canUseCache = false).toSingle())
            .doOnSuccess(joiningPoster::joinCourse) // interop with old code
            .doOnSuccess(enrollmentSubject::onNext) // notify everyone about changes

    private fun getCurrentProfileId(): Single<Long> =
        Single.fromCallable {
            sharedPreferenceHelper
                .profile
                ?.id
                ?: throw UNAUTHORIZED_EXCEPTION_STUB
        }

}