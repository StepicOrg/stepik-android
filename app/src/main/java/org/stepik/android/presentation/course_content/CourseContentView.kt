package org.stepik.android.presentation.course_content

import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class CourseContentLoaded(val courseContent: List<CourseContentItem>) : State()
        object NetworkError : State()
    }

    fun setState(state: State)

    fun updateSectionDownloadProgress(downloadProgress: DownloadProgress)
    fun updateUnitDownloadProgress(downloadProgress: DownloadProgress)
}