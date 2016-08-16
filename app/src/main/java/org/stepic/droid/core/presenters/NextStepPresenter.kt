package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.NextStepView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class NextStepPresenter(
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val databaseFacade: DatabaseFacade) : PresenterBase<NextStepView>() {

    /**
     * Last step in lesson can be shown differently
     */
    fun checkStepForLast(stepId: Long, lesson: Lesson, unit: Unit) {
        val stepIds = lesson.steps
        var isCandidateForShowing = false
        if (stepIds != null && stepIds.size != 0) {
            val lastStepId = stepIds[stepIds.size - 1]
            if (lastStepId == stepId) {
                //YEAH, it is candidate for showing -> check in db
                threadPoolExecutor.execute {
                    val section = databaseFacade.getSectionById(unit.section)
                    val units: LongArray? = section?.units
                    if (units != null && units.size > 0) {
                        val lastUnitId = units[units.size - 1]
                        if (lastUnitId != unit.id) {
                            mainHandler.post {
                                //if not last lesson in section -> show button
                            }
                        }
                    }
                }

            }
        }
    }

    fun clickNextLesson(currentLesson: Lesson) {

    }
}