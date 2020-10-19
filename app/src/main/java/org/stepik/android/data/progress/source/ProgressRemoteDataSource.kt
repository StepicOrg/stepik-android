package org.stepik.android.data.progress.source

import io.reactivex.Single
import org.stepik.android.model.Progress
import ru.nobird.android.domain.rx.first

interface ProgressRemoteDataSource {
    fun getProgress(progressId: String): Single<Progress> =
        getProgresses(listOf(progressId)).first()

    fun getProgresses(progressIds: List<String>): Single<List<Progress>>
}