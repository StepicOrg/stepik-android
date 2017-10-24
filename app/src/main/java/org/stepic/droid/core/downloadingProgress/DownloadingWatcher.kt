package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable

interface DownloadingWatcher {
    fun watch(id: Long): Flowable<Float>
}
