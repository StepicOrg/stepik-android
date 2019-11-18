package org.stepik.android.data.progress.source

import io.reactivex.Single
import org.stepik.android.model.Progress

interface ProgressRemoteDataSource {
    fun getProgress(progressId: String): Single<Progress>
    fun getProgresses(vararg progressIds: String): Single<List<Progress>>
}