package org.stepic.droid.core.presenters

import org.joda.time.DateTime
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class ContinueCoursePresenter(val databaseFacade: DatabaseFacade,
                              val api: IApi,
                              val threadPoolExecutor: ThreadPoolExecutor,
                              val mainHandler: IMainHandler) : PresenterBase<ContinueCourseView>() {

    val isHandling = AtomicBoolean(false)

    fun continueCourse(course: Course) {
        if (isHandling.compareAndSet(false, true)) {
            view?.onShowContinueCourseLoadingDialog()
            threadPoolExecutor.execute()
            {
                try {
                    databaseFacade.updateCourseLastInteraction(courseId = course.courseId, timestamp = DateTime.now().millis)
                    var unitId: Long
                    var stepId: Long
                    try {
                        val lastStep = api.getLastStepResponse(course.lastStepId!!).execute().body().lastSteps.first()
                        unitId = lastStep.unit
                        stepId = lastStep.step

                    } catch (exception: Exception) {
                        val persistentLastStep = databaseFacade.getLocalLastStepByCourseId(courseId = course.courseId)
                        unitId = persistentLastStep!!.unitId
                        stepId = persistentLastStep.stepId
                    }

                    var unit = databaseFacade.getUnitById(unitId)
                    if (unit == null) {
                        unit = api.getUnits(longArrayOf(unitId)).execute().body().units.first()
                    }
                    val sectionId = unit!!.section
                    var section = databaseFacade.getSectionById(sectionId)
                    if (section == null) {
                        section = api.getSections(longArrayOf(sectionId)).execute().body().sections.first()!!
                    }
                    val immutableSection: Section = section
                    val lessonId = unit.lesson

                    var step = databaseFacade.getStepById(stepId)
                    if (step == null) {
                        step = api.getSteps(longArrayOf(stepId)).execute().body().steps.first()!!
                    }

                    val stepPosition = step.position.toInt()

                    mainHandler.post {
                        view?.onOpenStep(courseId = course.courseId,
                                section = immutableSection,
                                lessonId = lessonId,
                                unitId = unitId,
                                stepPosition = stepPosition)
                    }
                } catch (exception: Exception) {
                    //connection problem || something is null -> try to resolve local
                    mainHandler.post {
                        view?.onAnyProblemWhileContinue(course)
                    }
                } finally {
                    isHandling.set(false)
                }
            }
        }
    }

}
