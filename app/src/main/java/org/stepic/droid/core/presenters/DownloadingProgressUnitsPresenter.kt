package org.stepic.droid.core.presenters

import org.stepic.droid.core.DownloadingProgressUnitPublisher
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.LessonLoadingState
import java.util.*

class DownloadingProgressUnitsPresenter(
        private val downloadingProgressUnitPublisher: DownloadingProgressUnitPublisher) : PresenterBase<DownloadingProgressUnitsView>() {

    private val downloadCallback = object : DownloadingProgressUnitPublisher.DownloadingProgressCallback {
        override fun onProgressChanged(lessonId: Long, newPortion: Float) {
            view?.onNewProgressValue(LessonLoadingState(lessonId, newPortion))
        }
    }

    private var isSubscribed = false


    fun subscribeToProgressUpdates(lessonList: List<Lesson>) {
        if (!isSubscribed && lessonList.isNotEmpty()) {
            val lessonStepsMap = HashMap<Long, Set<Long>>()
            lessonList.forEach {
                val steps = it.steps
                if (steps != null) {
                    lessonStepsMap[it.id] = steps.toSet()
                }
            }
            downloadingProgressUnitPublisher.subscribe(lessonStepsMap, downloadCallback)
            isSubscribed = true
        }
    }

    override fun detachView(view: DownloadingProgressUnitsView) {
        super.detachView(view)
        downloadingProgressUnitPublisher.unsubscribe()
        isSubscribed = false
    }
}
