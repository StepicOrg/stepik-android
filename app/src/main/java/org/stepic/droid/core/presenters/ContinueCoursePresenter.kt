package org.stepic.droid.core.presenters

import org.stepic.droid.adaptive.util.AdaptiveCoursesResolver
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.hasUserAccess
import org.stepic.droid.util.hasUserAccessAndNotEmpty
import org.stepic.droid.web.Api
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ContinueCoursePresenter
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val sectionRepository: Repository<Section>,
        private val adaptiveCoursesResolver: AdaptiveCoursesResolver
) : PresenterBase<ContinueCourseView>() {

    private val isHandling = AtomicBoolean(false)

    fun continueCourse(course: Course) {
        if (isHandling.compareAndSet(false, true)) {
            view?.onShowContinueCourseLoadingDialog()
            threadPoolExecutor.execute {
                try {
                    if (adaptiveCoursesResolver.isAdaptive(course.id)) {
                        mainHandler.post {
                            view?.onOpenAdaptiveCourse(course)
                        }
                        return@execute
                    }

                    var unitId: Long
                    var stepId: Long
                    try {
                        val lastStep = api.getLastStepResponse(course.lastStepId!!).execute().body()!!.lastSteps.first()
                        unitId = lastStep.unit!! //it can be null
                        stepId = lastStep.step!!// it can be null -> we should fetch 1st step
                    } catch (exception: Exception) {
                        val persistentLastStep = databaseFacade.getLocalLastStepByCourseId(courseId = course.id)
                        if (persistentLastStep == null) {
                            // fetch data
                            val sectionIds = course.sections
                            if (sectionIds?.isEmpty() != false) {
                                throw IllegalArgumentException("course without sections")
                            }
                            val section = sectionRepository
                                    .getObjects(sectionIds)
                                    .firstOrNull {
                                        it.hasUserAccessAndNotEmpty(course)
                                    }
                            val unit = fetchUnit(section?.units?.first()!!)
                            val lessonId = unit.lesson
                            // if server return Null on last step and local is not exist
                            mainHandler.post {
                                view?.onOpenStep(courseId = course.id,
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
                    if (!section.hasUserAccess(course)) {
                        throw IllegalAccessException("User doesn't have permission to this section ${section.id}")
                    }

                    val lessonId = unit.lesson

                    val step = fetchStep(stepId)!!

                    val stepPosition = step.position.toInt()

                    mainHandler.post {
                        view?.onOpenStep(courseId = course.id,
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
            unit = api.getUnits(listOf(unitId)).execute().body()?.units?.first()
        }
        return unit!! //if null -> should throw Exception
    }

    private fun fetchSection(sectionId: Long): Section {
        var section = databaseFacade.getSectionById(sectionId)
        if (section == null) {
            section = api.getSections(longArrayOf(sectionId)).execute().body()?.sections?.first()
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
