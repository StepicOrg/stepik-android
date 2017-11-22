package org.stepic.droid.core.downloadingstate

import io.reactivex.Flowable

interface ProgressWatcher {
    fun watch(id: Long): Flowable<Float>
}
