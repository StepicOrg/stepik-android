package org.stepic.droid.core.downloadingProgress

import io.reactivex.Flowable

interface ProgressWatcher {
    fun watch(id: Long): Flowable<Float>
}
