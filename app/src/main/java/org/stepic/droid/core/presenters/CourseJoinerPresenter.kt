package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.core.presenters.contracts.CourseJoinView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepik.android.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.web.Api
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class CourseJoinerPresenter
@Inject constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val joiningPoster: JoiningPoster,
        private val database: DatabaseFacade,
        private val analytic: Analytic
) : PresenterBase<CourseJoinView>() {

    // todo remove
    @MainThread
    fun joinCourse(course: Course) {
//        val response = sharedPreferenceHelper.authResponseFromStore
//        if (response != null) {
//            view?.showProgress()
//            view?.setEnabledJoinButton(false)
//            threadPoolExecutor.execute {
//                try {
//                    val tryJoinCourseResponse = api.tryJoinCourse(course).execute()
//                    if (tryJoinCourseResponse.isSuccessful) {
//                        handleSuccessResponse(course)
//                    } else {
//                        mainHandler.post {
//                            view?.onFailJoin(tryJoinCourseResponse.code())
//                        }
//                    }
//                } catch (exception: Exception) {
//                    //no internet
//                    if (exception !is IOException) {
//                        analytic.reportError(Analytic.Error.JOIN_FAILED, exception)
//                    }
//                    mainHandler.post {
//                        view?.onFailJoin(0)
//                    }
//                }
//            }
//        } else {
//            analytic.reportEvent(Analytic.Anonymous.JOIN_COURSE)
//            view?.onFailJoin(HttpURLConnection.HTTP_UNAUTHORIZED)
//        }
    }

    @WorkerThread
    private fun handleSuccessResponse(course: Course) {
        course.enrollment = course.id.toInt()


        mainHandler.post {
            joiningPoster.joinCourse(course)
            view?.onSuccessJoin(course)
        }

        //update in database
        database.addCourse(course, Table.enrolled)
        val isFeatured = database.getCourseById(course.id, Table.featured) != null
        if (isFeatured) {
            database.addCourse(course, Table.featured)
        }
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
