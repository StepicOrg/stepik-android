package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import com.squareup.otto.Bus
import org.joda.time.DateTime
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.contracts.CourseJoinView
import org.stepic.droid.events.joining_course.FailJoinEvent
import org.stepic.droid.events.joining_course.SuccessJoinEvent
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import org.stepic.droid.web.IApi
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.net.HttpURLConnection
import java.util.concurrent.ThreadPoolExecutor

class CourseJoinerPresenter(
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val api: IApi,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val bus: Bus,
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

                override fun onResponse(response: Response<Void>, retrofit: Retrofit) {
                    if (response.isSuccess) {

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

                        bus.post(SuccessJoinEvent(localCourseCopy)) //todo remake without bus
                        view?.onSuccessJoin(SuccessJoinEvent(localCourseCopy))
                    } else {
                        view?.onFailJoin(FailJoinEvent(response.code()))
                    }
                }

                override fun onFailure(t: Throwable) {
                    view?.onFailJoin(FailJoinEvent())
                }
            })
        } else {
            analytic.reportEvent(Analytic.Anonymous.JOIN_COURSE)
            view?.onFailJoin(FailJoinEvent(HttpURLConnection.HTTP_UNAUTHORIZED))
        }
    }

}
