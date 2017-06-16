package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.LoadCourseView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.web.Api
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
                    view?.onCourseFound(finalCourse)
                }
            } else {
                try {
                    val response = api.getCourse(courseId).execute()
                    if (response != null && response.isSuccessful && response.body().courses.isNotEmpty()) {
                        mainHandler.post {
                            view?.onCourseFound(response.body().courses.first())
                        }
                    } else {
                        mainHandler.post {
                            view?.onCourseUnavailable(courseId)
                        }
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        view?.onInternetFailWhenCourseIsTriedToLoad()
                    }
                }
            }
        }
    }
}
