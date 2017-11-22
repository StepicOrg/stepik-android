package org.stepic.droid.core.downloadingstate

interface DownloadingView {
    fun onNewProgressValue(id: Long, portion: Float)
}
