package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.LoadCourseView
import org.stepic.droid.events.courses.CourseCantLoadEvent
import org.stepic.droid.events.courses.CourseFoundEvent
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ThreadPoolExecutor

class CourseFinderPresenter(
        val threadPoolExecutor: ThreadPoolExecutor,
        val databaseFacade: DatabaseFacade,
        var api: IApi,
        val mainHandler: IMainHandler) : PresenterBase<LoadCourseView>() {

    fun findCourseById(courseId: Long) {
        threadPoolExecutor.execute {
            var course = databaseFacade.getCourseById(courseId, Table.featured)
            if (course == null) {
                course = databaseFacade.getCourseById(courseId, Table.enrolled)
            }

            val finalCourse = course
            if (finalCourse != null) {
                mainHandler.post {
                    view?.onCourseFound(CourseFoundEvent(finalCourse))
                }
            } else {
                api.getCourse(courseId).enqueue(object : Callback<CoursesStepicResponse> {

                    override fun onResponse(call: Call<CoursesStepicResponse>?, response: Response<CoursesStepicResponse>?) {
                        if (response != null && response.isSuccessful && !response.body().courses.isEmpty()) {
                            view?.onCourseFound(CourseFoundEvent(response.body().courses[0]))
                        } else {
                            view?.onCourseUnavailable(CourseUnavailableForUserEvent(courseId))

                        }
                    }

                    override fun onFailure(call: Call<CoursesStepicResponse>?, t: Throwable?) {
                        view!!.onInternetFailWhenCourseIsTriedToLoad(CourseCantLoadEvent())
                    }
                })
            }
        }
    }
}
