package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.persistence.model.DownloadItem
import org.stepik.android.model.Video

interface DownloadsView {
    fun addActiveDownload(downloadItem: DownloadItem)
    fun addCompletedDownload(downloadItem: DownloadItem)
    fun removeDownload(downloadItem: DownloadItem)

    fun showEmptyAuth()
    fun invalidateEmptyDownloads()

    fun showVideo(video: Video)

    fun askToCancelAllVideos()
    fun askToRemoveAllCachedVideos()

    fun showLoading()
    fun hideLoading()
    fun onCantRemoveVideo()
}