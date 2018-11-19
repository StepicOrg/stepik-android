package org.stepik.android.data.progress.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.model.Progress

interface ProgressCacheDataSource {
    fun getProgress(progressId: String): Maybe<Progress>
    fun saveProgress(progress: Progress): Completable
}