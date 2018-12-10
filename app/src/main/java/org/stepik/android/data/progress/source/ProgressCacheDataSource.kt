package org.stepik.android.data.progress.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Progress

interface ProgressCacheDataSource {
    fun getProgress(progressId: String): Maybe<Progress>
    fun getProgresses(vararg progressIds: String): Single<List<Progress>>

    fun saveProgress(progress: Progress): Completable
    fun saveProgresses(progresses: List<Progress>): Completable
}