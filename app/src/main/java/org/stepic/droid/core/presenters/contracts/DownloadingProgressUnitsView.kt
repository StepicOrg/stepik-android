package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.LessonLoadingState

interface DownloadingProgressUnitsView {
    fun onNewProgressValue(lessonLoadingState: LessonLoadingState)
}
