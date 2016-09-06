package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.StepsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Unit
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import org.stepic.droid.web.LessonStepicResponse
import retrofit.Response
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class StepsPresenter(val threadPoolExecutor: ThreadPoolExecutor,
                     val mainHandler: IMainHandler,
                     val databaseFacade: DatabaseFacade,
                     val api: IApi) : PresenterBase<StepsView>() {

    var lesson: Lesson? = null
        private set

    var isLoading = AtomicBoolean(false)

    var unit: Unit? = null


    fun init(outLesson: Lesson?, outUnit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPositionStartWithOne: Long) {
        if (this.lesson != null) {
            //already loaded if THIS.Lesson != null
            return
        }

        if (isLoading.get()) {
            return
        }

        isLoading.set(true)

        lesson = outLesson;
        unit = outUnit
        threadPoolExecutor.execute {
            try {
                if (lesson == null || unit == null) {
                    initUnitLessonWithIds(simpleLessonId, simpleUnitId)
                }

                //after that Lesson should be not null
                if (lesson == null) {
                    return@execute
                }
                mainHandler.post {
                    view?.onLessonUnitPrepared(lesson, unit)
                }




                //view?.onLessonPrepared
                //showSteps
            } finally {
                isLoading.set(false)
            }
        }
    }

    private fun initUnitLessonWithIds(simpleLessonId: Long, simpleUnitId: Long) {
        if (simpleLessonId < 0) {
            mainHandler.post {
                view?.onLessonCorrupted()
            }
            return
        }

        lesson = databaseFacade.getLessonById(simpleLessonId)
        if (lesson == null) {
            //not in database yet
            var response: Response<LessonStepicResponse?>? = null
            try {
                response = api.getLessons(longArrayOf(simpleLessonId)).execute()
            } catch (ex: Exception) {
                mainHandler.post {
                    view?.onConnectionProblem()
                }
                return
            }

            try {
                lesson = response?.body()?.lessons?.firstOrNull()
                if (lesson == null) {
                    mainHandler.post {
                        view?.onLessonCorrupted()
                    }
                    return
                } else {
                    lesson?.let {
                        databaseFacade.addLesson(it)
                    }
                }
            } catch (ex: Exception) {
                mainHandler.post {
                    view?.onLessonCorrupted()
                }
                return
            }
        }

        //now lesson is parsed. Try to parse optional unit

        if (simpleUnitId >= 0) {
            unit = databaseFacade.getUnitById(simpleUnitId)
        }

        if (unit == null) {
            if (simpleUnitId >= 0) {
                //get by unitId
                try {
                    unit = api.getUnits(longArrayOf(simpleUnitId)).execute()?.body()?.units?.firstOrNull();
                } catch (ignored: Exception) {
                    //fail load: ok, we don't have unit
                }
                if (!(unit?.lesson?.equals(simpleLessonId) ?: false)) {
                    //if lesson is not equal unit.lesson or something null
                    loadUnitByLessonId(simpleLessonId)
                }
            } else {
                loadUnitByLessonId(simpleLessonId)
            }

            if (!(unit?.lesson?.equals(simpleLessonId) ?: false)) {
                unit = null
            }
        }
    }

    private fun loadUnitByLessonId(simpleLessonId: Long) {
        try {
            unit = api.getUnitByLessonId(simpleLessonId).execute()?.body()?.units?.firstOrNull();
        } catch (ignored: Exception) {
            //fail load: ok, we don't have unit
        }
    }


    private fun showSteps() {
        //todo get from cache -> show if not empty -> update from api -> show if you can (handle if steps updated)
    }

}
