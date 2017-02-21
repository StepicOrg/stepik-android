package org.stepic.droid.core.presenters

import org.stepic.droid.core.DownloadingProgressPublisher
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.LessonLoadingState
import java.util.*

class DownloadingProgressUnitsPresenter(
        private val downloadingProgressPublisher: DownloadingProgressPublisher) : PresenterBase<DownloadingProgressUnitsView>() {

    private val downloadCallback = object : DownloadingProgressPublisher.DownloadingProgressCallback {
        override fun onProgressChanged(lessonId: Long, newPortion: Float) {
            view?.onNewProgressValue(LessonLoadingState(lessonId, newPortion))
        }
    }


    fun subscribeToProgressUpdates(lessonList: List<Lesson>) {
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
