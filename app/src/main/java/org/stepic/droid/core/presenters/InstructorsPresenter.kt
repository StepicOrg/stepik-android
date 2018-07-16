package org.stepic.droid.core.presenters

import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.InstructorsView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.Course
import org.stepik.android.model.User
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@CourseAndSectionsScope
class InstructorsPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val api: Api
) : PresenterBase<InstructorsView>() {

    private val cachedInstructors = ArrayList<User>()
    private var cachedCourseId: Long = -1L
    private val isLoading = AtomicBoolean(false)


    fun fetchInstructors(course: Course?) {
        if (course == null || course.instructors?.isEmpty() ?: true) {
            view?.onHideInstructors()
            return
        }

        if (course.courseId == cachedCourseId) {
            if (cachedInstructors.isEmpty()) {
                view?.onHideInstructors()
            } else {
                view?.onInstructorsLoaded(cachedInstructors)
            }
            return
        }

        if (isLoading.compareAndSet(false, true)) {
            view?.onLoadingInstructors()
            threadPoolExecutor.execute {
                try {
                    //here course.instructors are not empty
                    fetchInstructorsInternally(course)
                } finally {
                    isLoading.set(false)
                }

            }
        }
    }

    @WorkerThread
    private fun fetchInstructorsInternally(course: Course) {
        try {
            val instructorList =
                    api
                            .getUsers(course.instructors)
                            .execute()
                            .body()
                            ?.users ?: throw NullPointerException("instructors are null on server")
            mainHandler.post {
                cachedCourseId = course.courseId
                cachedInstructors.clear()
                if (instructorList.isEmpty()) {
                    view?.onHideInstructors()
                } else {
                    cachedInstructors.addAll(instructorList)
                    view?.onInstructorsLoaded(cachedInstructors)
                }
            }
        } catch (exception: Exception) {
            view?.onFailLoadInstructors()
        }
    }
}
