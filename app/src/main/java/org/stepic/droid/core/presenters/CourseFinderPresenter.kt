package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.LoadCourseView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.events.courses.CourseCantLoadEvent
import org.stepic.droid.events.courses.CourseFoundEvent
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.web.Api
import org.stepic.droid.web.CoursesStepicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class CourseFinderPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val databaseFacade: DatabaseFacade,
        private var api: Api,
        private val mainHandler: MainHandler) : PresenterBase<LoadCourseView>() {

    fun findCourseById(courseId: Long) {
        threadPoolExecutor.execute {
            var course = databaseFacade.getCourseById(courseId, Table.enrolled)
            if (course == null) {
                course = databaseFacade.getCourseById(courseId, Table.featured)
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
