package org.stepik.android.presentation.download.mapper

import org.stepic.droid.persistence.model.DownloadItem
import org.stepik.android.presentation.download.DownloadView
import javax.inject.Inject

class DownloadItemsStateMapper
@Inject
constructor() {
    fun addDownloadItem(state: DownloadView.State.DownloadedCoursesLoaded, downloadItem: DownloadItem): DownloadView.State.DownloadedCoursesLoaded =
        DownloadView.State.DownloadedCoursesLoaded(state.courses + downloadItem)

    fun removeDownloadItem(state: DownloadView.State.DownloadedCoursesLoaded, downloadItem: DownloadItem): DownloadView.State.DownloadedCoursesLoaded =
        DownloadView.State.DownloadedCoursesLoaded(state.courses - downloadItem)
}