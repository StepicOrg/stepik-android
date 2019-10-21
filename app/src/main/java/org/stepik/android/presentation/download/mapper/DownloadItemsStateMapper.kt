package org.stepik.android.presentation.download.mapper

import org.stepic.droid.persistence.model.DownloadItem
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.presentation.download.DownloadView
import timber.log.Timber
import javax.inject.Inject

class DownloadItemsStateMapper
@Inject
constructor() {
    fun addDownloadItem(state: DownloadView.State.DownloadedCoursesLoaded, downloadItem: DownloadItem): DownloadView.State.DownloadedCoursesLoaded {
        val courses = state.courses
        val item = courses.find { it.course.id == downloadItem.course.id }
        if (downloadItem.course.id == 76L) {
            Timber.d("Download item: $downloadItem")
        }
        return if (item == null) {
            DownloadView.State.DownloadedCoursesLoaded(state.courses + downloadItem)
        } else {
            val oldBytes = (item.status as DownloadProgress.Status.Cached).bytesTotal
            val newBytes = (downloadItem.status as DownloadProgress.Status.Cached).bytesTotal
            val a = courses - item
            val b = a + downloadItem.copy(status = DownloadProgress.Status.Cached(oldBytes + newBytes))
            DownloadView.State.DownloadedCoursesLoaded(b)
        }
    }
}