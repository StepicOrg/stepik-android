package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.persistence.model.DownloadItem

interface DownloadsView {
    fun addActiveDownload(downloadItem: DownloadItem)
    fun addCompletedDonwload(downloadItem: DownloadItem)
    fun removeDownload(downloadItem: DownloadItem)
}