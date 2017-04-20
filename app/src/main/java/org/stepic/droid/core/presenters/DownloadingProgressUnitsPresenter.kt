package org.stepic.droid.core.presenters

import org.stepic.droid.core.DownloadingProgressUnitsPublisher
import org.stepic.droid.core.presenters.contracts.DownloadingProgressUnitsView
import org.stepic.droid.di.section.SectionScope
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.LessonLoadingState
import java.util.*
import javax.inject.Inject

@SectionScope
class DownloadingProgressUnitsPresenter
@Inject constructor(
        private val downloadingProgressUnitPublisher: DownloadingProgressUnitsPublisher) : PresenterBase<DownloadingProgressUnitsView>() {

    private val downloadCallback = object : DownloadingProgressUnitsPublisher.DownloadingProgressCallback {
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
