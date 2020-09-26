package org.stepik.android.data.progress.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.model.Progress

interface ProgressCacheDataSource {
    fun getProgress(progressId: String): Maybe<Progress> =
        getProgresses(listOf(progressId)).maybeFirst()

    fun getProgresses(progressIds: List<String>): Single<List<Progress>>

    fun saveProgress(progress: Progress): Completable =
        saveProgresses(listOf(progress))

    fun saveProgresses(progresses: List<Progress>): Completable
}