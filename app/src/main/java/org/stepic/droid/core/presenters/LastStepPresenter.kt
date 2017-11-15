package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.FirstStepInCourseHelper
import org.stepic.droid.core.presenters.contracts.LastStepView
import org.stepic.droid.di.course_list.CourseListScope
import org.stepic.droid.model.Section
import org.stepic.droid.model.Step
import org.stepic.droid.model.Unit
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.hasUserAccess
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseListScope
class LastStepPresenter
@Inject
constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val databaseFacade: DatabaseFacade,
        private val api: Api,
        private val stepRepository: Repository<Step>,
        private val unitRepository: Repository<Unit>,
        private val sectionRepository: Repository<Section>,
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
                        val lastStep = api.getLastStepResponse(lastStepId).execute()?.body()?.lastSteps?.first()!!
                        when (checkAccessToUnit(lastStep.unit!!)) {
                            true -> lastStep.step
                            false -> throw RuntimeException("No access to section")
                            null -> throw RuntimeException("No connection")
                        }
                    } catch (exception: Exception) {
                        //no internet or no permission
                        //when no internet -> try get local
                        val localLastStepByCourseId = databaseFacade.getLocalLastStepByCourseId(courseId)
                        localLastStepByCourseId?.let {
                            if (checkAccessToUnit(it.unitId) == true) {
                                localLastStepByCourseId.stepId
                            } else {
                                null
                            }
                        }
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
        val stepId = firstStepInCourseHelper.getStepIdOfTheFirstAvailableStepInCourse(courseId) ?: return null
        return getStep(stepId)
    }

    private fun getStep(stepId: Long): Step? = stepRepository.getObject(stepId)

    private fun checkAccessToUnit(unitId: Long): Boolean? {
        val unit = unitRepository.getObject(unitId) ?: return null
        val section = sectionRepository.getObject(unit.section) ?: return null
        return section.hasUserAccess()
    }
}
