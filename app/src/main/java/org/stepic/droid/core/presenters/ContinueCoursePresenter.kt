package org.stepic.droid.core.presenters

import org.joda.time.DateTime
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.model.Step
import org.stepic.droid.model.Unit
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class ContinueCoursePresenter(
        private val databaseFacade: DatabaseFacade,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler) : PresenterBase<ContinueCourseView>() {

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
                        unitId = lastStep.unit!!
                        stepId = lastStep.step!!

                    } catch (exception: Exception) {
                        val persistentLastStep = databaseFacade.getLocalLastStepByCourseId(courseId = course.courseId)
                        if (persistentLastStep == null) {
                            // fetch data

                            val sectionId = course.sections.first()
                            val section = fetchSection(sectionId)
                            val unit = fetchUnit(section.units?.first()!!)
                            val lessonId = unit.lesson
                            // if server return Null on last step and local is not exist
                            mainHandler.post {
                                view?.onOpenStep(courseId = course.courseId,
                                        section = section,
                                        lessonId = lessonId,
                                        unitId = unit.id,
                                        stepPosition = 1)
                            }
                            return@execute
                        }
                        unitId = persistentLastStep.unitId
                        stepId = persistentLastStep.stepId
                    }

                    val unit = fetchUnit(unitId)
                    val sectionId = unit.section

                    val section = fetchSection(sectionId)
                    val lessonId = unit.lesson

                    val step = fetchStep(stepId)!!

                    val stepPosition = step.position.toInt()

                    mainHandler.post {
                        view?.onOpenStep(courseId = course.courseId,
                                section = section,
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

    private fun fetchUnit(unitId: Long): Unit {
        var unit = databaseFacade.getUnitById(unitId)
        if (unit == null) {
            unit = api.getUnits(longArrayOf(unitId)).execute().body().units.first()
        }
        return unit!! //if null -> should throw Exception
    }

    private fun fetchSection(sectionId: Long): Section {
        var section = databaseFacade.getSectionById(sectionId)
        if (section == null) {
            section = api.getSections(longArrayOf(sectionId)).execute().body().sections.first()
        }
        return section!!
    }

    private fun fetchStep(stepId: Long): Step? {
        var step = databaseFacade.getStepById(stepId)
        if (step == null) {
            step = api.getSteps(longArrayOf(stepId)).execute().body()?.steps?.first()
        }
        return step
    }

}
