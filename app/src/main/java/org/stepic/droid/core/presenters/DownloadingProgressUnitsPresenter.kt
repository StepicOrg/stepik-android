package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.DownloadingProgressPublisher
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.LessonLoadingState
import org.stepic.droid.store.operations.DatabaseFacade
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

class DownloadingProgressUnitsPresenter(
        private val downloadingProgressPublisher: DownloadingProgressPublisher,
        private val databaseFacade: DatabaseFacade,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: IMainHandler) : PresenterBase<DownloadingProgressUnitsView>() {

    private val downloadCallback = object : DownloadingProgressPublisher.DownloadingProgressCallback {
        override fun onProgressChanged(lessonId: Long, newPortion: Float) {
            view?.onNewProgressValue(LessonLoadingState(lessonId, newPortion))
        }

//        val stepIdPortionConcurrentMap = ConcurrentHashMap<Long, Float>()
//
//        @MainThread
//        override fun onProgressChanged(downloadEntity: DownloadEntity, newPortion: Float) {
//            threadPoolExecutor.execute {
//                val step = databaseFacade.getStepById(downloadEntity.stepId) ?: return@execute
//                val videoStepList = databaseFacade
//                        .getStepsOfLesson(step.lesson)
//                        .filter { it?.block?.name == AppConstants.TYPE_VIDEO }
//                        .filterNotNull()
//
//                var thisWasUpdated = false
//                var currentProgress = 0.toFloat()
//                videoStepList.forEach {
//                    if (downloadEntity.stepId == it.id) {
//                        thisWasUpdated = true
//                        currentProgress += newPortion
//                        stepIdPortionConcurrentMap.put(downloadEntity.stepId, newPortion)
//                    } else if (it.is_cached) {
//                        currentProgress += 1;
//                    } else {
//                        val cachedPortion: Float? = stepIdPortionConcurrentMap[downloadEntity.stepId]
//                        if (cachedPortion != null) {
//                            currentProgress += cachedPortion
//                        }
//                    }
//                }
//
//                val progressToPublish = currentProgress / videoStepList.size
//                val lessonId = videoStepList.firstOrNull()?.lesson
//                if (lessonId != null && thisWasUpdated) {
//                    mainHandler.post {
//                        if (lessonIds.contains(lessonId)) {
//                            view?.onNewProgressValue(LessonLoadingState(lessonId = lessonId, portion = progressToPublish))
//                        }
//                    }
//                }
//
//            }
//        }
    }

//    val lessonIds = HashSet<Long>()

    fun subscribeToProgressUpdates(lessonList: List<Lesson>) {
//        lessonIds.clear()
//        lessonIds.addAll(lessonList.map(Lesson::id).toSet())

        val lessonStepsMap = HashMap<Long, Set<Long>>()
        lessonList.forEach {
            val steps = it.steps
            if (steps != null) {
                lessonStepsMap[it.id] = steps.toSet()
            }
        }
        downloadingProgressPublisher.subscribe(lessonStepsMap, downloadCallback)
    }

    override fun detachView(view: DownloadingProgressUnitsView) {
        super.detachView(view)
        downloadingProgressPublisher.unsubscribe()
    }
}
