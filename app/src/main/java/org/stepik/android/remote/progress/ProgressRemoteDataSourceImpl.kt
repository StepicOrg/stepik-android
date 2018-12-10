package org.stepik.android.remote.progress

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepic.droid.web.ProgressesResponse
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : ProgressRemoteDataSource {
    override fun getProgress(progressId: String): Single<Progress> =
        getProgresses(progressId)
            .map { it.first() }

    override fun getProgresses(vararg progressIds: String): Single<List<Progress>> =
        if (progressIds.isEmpty()) {
            Single.just(emptyList())
        } else {
            api.getProgressesReactive(progressIds)
                .map(ProgressesResponse::getProgresses)
        }
}