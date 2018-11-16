package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.dropping.contract.DroppingPoster
import org.stepic.droid.core.presenters.contracts.DroppingView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.features.deadlines.repository.DeadlinesRepository
import org.stepik.android.model.Course
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
        private val databaseFacade: DatabaseFacade,

        private val deadlinesRepository: DeadlinesRepository
) : PresenterBase<DroppingView>() {

    fun dropCourse(course: Course) {
        threadPoolExecutor.execute {
            val dropCall = api.dropCourse(course)
            if (dropCall == null) {
                mainHandler.post {
                    view?.onUserHasNotPermissionsToDrop()
                }
            } else {
                try {
                    makeDropCall(dropCall, course)
                    deadlinesRepository.removeDeadlinesForCourse(course.id).blockingAwait()
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
        if (databaseFacade.getCourseById(course.id, Table.featured) != null) {
            course.enrollment = 0
            databaseFacade.addCourse(course, Table.featured)
        }
    }
}
