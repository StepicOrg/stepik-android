package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.FirstStepInCourseHelper
import org.stepic.droid.core.presenters.contracts.LastStepView
import org.stepic.droid.di.course_list.CourseGeneralScope
import org.stepic.droid.model.Step
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseGeneralScope
class LastStepPresenter
@Inject
constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val databaseFacade: DatabaseFacade,
        private val api: Api,
        private val stepRepository: Repository<Step>,
        private val firstStepInCourseHelper: FirstStepInCourseHelper
) : PresenterBase<LastStepView>() {

    @MainThread
    fun fetchLastStep(lastStepId: String?, courseId: Long) {
        if (lastStepId == null) {
            view?.onShowPlaceholder()
            return
        }

        threadPoolExecutor.execute {
            val stepId: Long? =
                    try {
                        api.getLastStepResponse(lastStepId).execute()?.body()?.lastSteps?.first()?.step
                    } catch (exception: Exception) {
                        //no internet or no permission
                        //when no internet -> try get local
                        databaseFacade.getLocalLastStepByCourseId(courseId)?.stepId
                    }

            val step: Step? =
                    if (stepId == null) {
                        //lets try to open the 1st step in course
                        getFirstStepInCourse(courseId)
                    } else {
                        getStep(stepId)
                    }

            with(mainHandler) {
                if (step != null) {
                    post { view?.onShowLastStep(step) }
                } else {
                    post { view?.onShowPlaceholder() }
                }
            }
        }
    }


    private fun getFirstStepInCourse(courseId: Long): Step? {
        val stepId = firstStepInCourseHelper.getStepIdOfTheFirstStepInCourse(courseId) ?: return null
        return getStep(stepId)
    }

    private fun getStep(stepId: Long): Step? = stepRepository.getObject(stepId)
}
