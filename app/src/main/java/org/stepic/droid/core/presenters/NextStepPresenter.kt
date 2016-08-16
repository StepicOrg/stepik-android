package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.NextStepView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.concurrent.ThreadPoolExecutor

class NextStepPresenter(
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val databaseFacade: DatabaseFacade,
        val analytic: Analytic) : PresenterBase<NextStepView>() {

    /**
     * Last step in lesson can be shown differently
     */
    fun checkStepForLast(stepId: Long, lesson: Lesson, unit: Unit) {
        val stepIds = lesson.steps
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
                                view?.showNextLessonView()
                            }
                        }
                    }
                }

            }
        }
    }

    fun clickNextLesson(currentLesson: Lesson, unit: Unit) {
        analytic.reportEvent(Analytic.Interaction.CLICK_NEXT_LESSON_IN_STEPS)
        view?.showLoadDialog()
        threadPoolExecutor.execute {
            val section = databaseFacade.getSectionById(unit.section)
            var nextUnitId: Long? = null

            val unitIds = section?.units
            val numberOfUnits = unitIds?.size?:0
            unitIds?.forEachIndexed { index, unitId ->
                if (unit.id == unitId && index + 1 < numberOfUnits) {
                    nextUnitId = unitIds[index + 1]
                    return@forEachIndexed
                }
            }
            if (nextUnitId != null) {
                val nextUnit = databaseFacade.getUnitById(nextUnitId!!)
                if (nextUnit != null) {
                    val nextLesson = databaseFacade.getLessonById(unit.lesson)
                    if (nextLesson != null) {
                        mainHandler.post {
                            view?.openNextLesson(nextUnit, nextLesson)
                        }
                        return@execute
                    }
                }
            }


            //if someone is null -> show error
            analytic.reportError(Analytic.Error.ILLEGAL_STATE_NEXT_LESSON, IllegalStateNextLessonException(unit.id))
            mainHandler.post {
                view?.showCantGoNext()
            }

        }
    }

    inner class IllegalStateNextLessonException : IllegalStateException {
        constructor(unitId: Long) : super("Next lesson is shouldn't be shown, unitId = " + unitId.toString())
    }
}