package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.core.presenters.contracts.DroppingView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.model.Course
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.web.Api
import retrofit2.Call
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseListScope
class DroppingPresenter
@Inject
constructor(
        private val droppingPoster: DroppingPoster,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val databaseFacade: DatabaseFacade
) : PresenterBase<DroppingView>() {

    private val deadlinesRepository = api.provideDeadlineRepository()

    fun dropCourse(course: Course) {
        threadPoolExecutor.execute {
            deadlinesRepository.removeDeadlinesForCourse(course.courseId).blockingAwait()
            val dropCall = api.dropCourse(course.courseId)
            if (dropCall == null) {
                mainHandler.post {
                    view?.onUserHasNotPermissionsToDrop()
                }
            } else {
                try {
                    makeDropCall(dropCall, course)
                } catch (exception: Exception) {
                    mainHandler.post {
                        droppingPoster.failDropCourse(course)
                    }
                }
            }
        }
    }

    @WorkerThread
    private fun makeDropCall(dropCall: Call<Void>, course: Course) {
        val dropResponse = dropCall.execute()
        if (dropResponse.isSuccessful) {
            databaseFacade.deleteCourse(course, Table.enrolled)
            rewriteEnrollmentInFeaturedIfNeeded(course)
            mainHandler.post {
                droppingPoster.successDropCourse(course)
            }
        } else {
            mainHandler.post {
                droppingPoster.failDropCourse(course)
            }
        }
    }

    @WorkerThread
    private fun rewriteEnrollmentInFeaturedIfNeeded(course: Course) {
        if (databaseFacade.getCourseById(course.courseId, Table.featured) != null) {
            course.enrollment = 0
            databaseFacade.addCourse(course, Table.featured)
        }
    }
}
