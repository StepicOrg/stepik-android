package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.joda.time.DateTime
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.joining.contract.JoiningPoster
import org.stepic.droid.core.presenters.contracts.CourseJoinView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.web.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class CourseJoinerPresenter
@Inject constructor(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val joiningPoster: JoiningPoster,
        private val database: DatabaseFacade,
        private val analytic: Analytic) : PresenterBase<CourseJoinView>() {

    @MainThread
    fun joinCourse(course: Course) {
        val response = sharedPreferenceHelper.authResponseFromStore
        if (response != null) {
            view?.showProgress()
            view?.setEnabledJoinButton(false)

            api.tryJoinCourse(course).enqueue(object : Callback<Void> {

                private val localCourseCopy = course

                override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                    if (response?.isSuccessful ?: false) {
                        localCourseCopy.enrollment = localCourseCopy.courseId.toInt()

                        threadPoolExecutor.execute {
                            //update in database
                            database.addCourse(localCourseCopy, Table.enrolled)
                            val isFeatured = database.getCourseById(localCourseCopy.courseId, Table.featured) != null
                            if (isFeatured) {
                                database.addCourse(localCourseCopy, Table.featured)
                            }
                            val enrollNotificationClickMillis: Long? = sharedPreferenceHelper.lastClickEnrollNotification
                            enrollNotificationClickMillis?.let {
                                val wasClicked = DateTime(it)
                                if (wasClicked.plusMinutes(30).isAfterNow) {
                                    sharedPreferenceHelper.clickEnrollNotification(-1L)
                                    analytic.reportEvent(Analytic.Notification.REMIND_ENROLL)
                                }
                            }
                        }

                        joiningPoster.joinCourse(localCourseCopy)
                        view?.onSuccessJoin(localCourseCopy)
                    } else {
                        view?.onFailJoin(response?.code() ?: 0)
                    }
                }

                override fun onFailure(call: Call<Void>?, t: Throwable?) {
                    view?.onFailJoin(0)
                }
            })
        } else {
            analytic.reportEvent(Analytic.Anonymous.JOIN_COURSE)
            view?.onFailJoin(HttpURLConnection.HTTP_UNAUTHORIZED)
        }
    }

}
