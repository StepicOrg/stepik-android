package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.persistence.model.DownloadItem

interface DownloadsView {
    fun addActiveDownload(downloadItem: DownloadItem)
    fun addCompletedDownload(downloadItem: DownloadItem)
    fun removeDownload(downloadItem: DownloadItem)

    fun showEmptyAuth()
    fun invalidateEmptyDownloads()
}