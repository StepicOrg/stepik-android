package org.stepic.droid.core.downloadingprogress

interface DownloadingView {
    fun onNewProgressValue(id: Long, portion: Float)
}
