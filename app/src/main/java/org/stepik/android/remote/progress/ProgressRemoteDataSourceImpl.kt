package org.stepik.android.remote.progress

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : ProgressRemoteDataSource {
    override fun getProgress(progressId: String): Single<Progress> =
            api.getProgressesReactive(arrayOf(progressId))
                .map { it.progresses.first() }
}