package org.stepic.droid.ui.presenters.course_joiner

import com.squareup.otto.Bus
import org.stepic.droid.base.MainApplication
import org.stepic.droid.concurrency.tasks.UpdateCourseTask
import org.stepic.droid.events.joining_course.FailJoinEvent
import org.stepic.droid.events.joining_course.SuccessJoinEvent
import org.stepic.droid.model.Course
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.ui.abstraction.CourseJoinView
import org.stepic.droid.ui.presenters.PresenterImpl
import org.stepic.droid.web.IApi
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.net.HttpURLConnection
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class CourseJoinerPresenterImpl : PresenterImpl<CourseJoinView>(), CourseJoinerPresenter {
    @Inject
    lateinit var mSharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var api: IApi

    @Inject
    lateinit var mThreadPoolExecutor: ThreadPoolExecutor

    @Inject
    lateinit var bus: Bus

    init {
        MainApplication.component().inject(this)
    }

    override fun joinCourse(mCourse: Course) {
        val response = mSharedPreferenceHelper.authResponseFromStore
        if (response != null) {
            view?.showProgress()
            view?.setEnabledJoinButton(false)

            api.tryJoinCourse(mCourse).enqueue(object : Callback<Void> {
                private val localCopy = mCourse

                override fun onResponse(response: Response<Void>, retrofit: Retrofit) {
                    if (response.isSuccess) {

                        localCopy.enrollment = localCopy.courseId.toInt()

                        val updateCourseTask = UpdateCourseTask(DatabaseFacade.Table.enrolled, localCopy)
                        updateCourseTask.executeOnExecutor(mThreadPoolExecutor)

                        val updateCourseFeaturedTask = UpdateCourseTask(DatabaseFacade.Table.featured, localCopy)
                        updateCourseFeaturedTask.executeOnExecutor(mThreadPoolExecutor)

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
