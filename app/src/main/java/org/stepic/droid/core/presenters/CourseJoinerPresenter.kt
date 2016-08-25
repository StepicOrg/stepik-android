package org.stepic.droid.core.presenters

import com.squareup.otto.Bus
import org.stepic.droid.concurrency.tasks.UpdateCourseTask
import org.stepic.droid.core.presenters.contracts.CourseJoinView
import org.stepic.droid.events.joining_course.FailJoinEvent
import org.stepic.droid.events.joining_course.SuccessJoinEvent
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.net.HttpURLConnection
import java.util.concurrent.ThreadPoolExecutor

class CourseJoinerPresenter(
        val sharedPreferenceHelper: SharedPreferenceHelper,
        val api: IApi,
        val threadPoolExecutor: ThreadPoolExecutor,
        val bus: Bus
) : PresenterBase<CourseJoinView>() {

    fun joinCourse(mCourse: Course) {
        val response = sharedPreferenceHelper.authResponseFromStore
        if (response != null) {
            view?.showProgress()
            view?.setEnabledJoinButton(false)

            api.tryJoinCourse(mCourse).enqueue(object : Callback<Void> {
                private val localCopy = mCourse

                override fun onResponse(response: Response<Void>, retrofit: Retrofit) {
                    if (response.isSuccess) {

                        localCopy.enrollment = localCopy.courseId.toInt()

                        val updateCourseTask = UpdateCourseTask(DatabaseFacade.Table.enrolled, localCopy)
                        updateCourseTask.executeOnExecutor(threadPoolExecutor)

                        val updateCourseFeaturedTask = UpdateCourseTask(DatabaseFacade.Table.featured, localCopy)
                        updateCourseFeaturedTask.executeOnExecutor(threadPoolExecutor)

                        bus.post(SuccessJoinEvent(localCopy)) //todo reamke without bus
                        view?.onSuccessJoin(SuccessJoinEvent(localCopy))
                    } else {
                        view?.onFailJoin(FailJoinEvent(response.code()))
                    }
                }

                override fun onFailure(t: Throwable) {
                    view?.onFailJoin(FailJoinEvent())
                }
            })
        } else {
            view?.onFailJoin(FailJoinEvent(HttpURLConnection.HTTP_UNAUTHORIZED))
        }
    }

}
