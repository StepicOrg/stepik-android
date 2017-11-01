package org.stepic.droid.core.downloadingProgress

interface DownloadingView {
    fun onNewProgressValue(id: Long, portion: Float)
}
