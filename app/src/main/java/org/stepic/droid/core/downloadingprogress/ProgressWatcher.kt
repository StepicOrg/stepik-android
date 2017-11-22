package org.stepic.droid.core.downloadingprogress

import io.reactivex.Flowable

interface ProgressWatcher {
    fun watch(id: Long): Flowable<Float>
}
