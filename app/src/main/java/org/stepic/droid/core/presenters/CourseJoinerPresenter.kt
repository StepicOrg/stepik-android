package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.core.presenters.contracts.CourseJoinView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.CourseListType
import org.stepik.android.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.course.interactor.CourseEnrollmentInteractor
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class CourseJoinerPresenter
@Inject constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val joiningPoster: JoiningPoster,
        private val database: DatabaseFacade,
        private val analytic: Analytic,
        private val courseEnrollmentInteractor: CourseEnrollmentInteractor
) : PresenterBase<CourseJoinView>() {

    @MainThread
    fun joinCourse(course: Course) {
        view?.showProgress()
        view?.setEnabledJoinButton(false)
        threadPoolExecutor.execute {
            try {
                courseEnrollmentInteractor.enrollCourse(course.id).blockingAwait()
                handleSuccessResponse(course)
            } catch (exception: Exception) {
                //no internet
                if (exception !is IOException) {
                    analytic.reportError(Analytic.Error.JOIN_FAILED, exception)
                }
                val errorCode = (exception as? HttpException)?.code() ?: 0
                mainHandler.post {
                    view?.onFailJoin(errorCode)
                }
            }
        }
    }

    @WorkerThread
    private fun handleSuccessResponse(course: Course) {
        course.enrollment = course.id.toInt()

        mainHandler.post {
            joiningPoster.joinCourse(course)
            view?.onSuccessJoin(course)
        }

        //update in database
        database.addCourseList(CourseListType.ENROLLED, listOf(course))
        val enrollNotificationClickMillis: Long? = sharedPreferenceHelper.lastClickEnrollNotification
        enrollNotificationClickMillis?.let {
            val wasClickedPlus30Min = it + 30 * AppConstants.MILLIS_IN_1MINUTE
            if (DateTimeHelper.isAfterNowUtc(wasClickedPlus30Min)) {
                //if  now < wasClicked+30min -> event is related to click
                sharedPreferenceHelper.clickEnrollNotification(-1L)
                analytic.reportEvent(Analytic.Notification.REMIND_ENROLL)
            }
        }
    }

}
