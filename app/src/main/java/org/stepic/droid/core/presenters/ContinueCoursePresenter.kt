package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.model.Course
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import java.util.concurrent.ThreadPoolExecutor

class ContinueCoursePresenter(val databaseFacade: DatabaseFacade,
                              val api: IApi,
                              val threadPoolExecutor: ThreadPoolExecutor,
                              val mainHandler: IMainHandler) : PresenterBase<ContinueCourseView>() {

    fun continueCourse(course: Course) {
        threadPoolExecutor.execute {
            val section = databaseFacade.getSectionById(141)
            val lessonId = 3361L
            val unitId = 944L
            val stepPosition = 1
            mainHandler.post {
                view?.onOpenStep(courseId = 67,
                        section = section!!,
                        lessonId = lessonId,
                        unitId = unitId,
                        stepPosition = stepPosition
                )
            }
        }
    }

}
