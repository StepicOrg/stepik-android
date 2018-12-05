package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.LoadCourseView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.storage.operations.DatabaseFacade
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
            val finalCourse = databaseFacade.getCourseById(courseId)
            if (finalCourse != null) {
                mainHandler.post {
                    view?.onCourseFound(finalCourse)
                }
            } else {
                try {
                    val courseToPresent = api.getCourse(courseId).execute().body()?.courses?.firstOrNull()
                    if (courseToPresent != null) {
                        mainHandler.post {
                            view?.onCourseFound(courseToPresent)
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
