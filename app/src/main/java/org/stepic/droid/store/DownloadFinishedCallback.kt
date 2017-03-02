package org.stepic.droid.store

import org.stepic.droid.model.DownloadEntity

interface DownloadFinishedCallback {
    fun onDownloadCompleted(downloadEntity: DownloadEntity, isSuccess: Boolean)
}
