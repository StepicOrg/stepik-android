package org.stepik.android.remote.progress

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.model.Progress
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.progress.model.ProgressResponse
import javax.inject.Inject

class ProgressRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : ProgressRemoteDataSource {
    private val progressResponseMapper =
        Function<ProgressResponse, List<Progress>>(ProgressResponse::progresses)

    override fun getProgress(progressId: String): Single<Progress> =
        getProgresses(progressId)
            .map { it.first() }

    override fun getProgresses(vararg progressIds: String): Single<List<Progress>> =
        progressIds
            .chunkedSingleMap { ids ->
                api.getProgressesReactive(ids)
                    .map(progressResponseMapper)
            }
}