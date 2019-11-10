package org.stepik.android.remote.progress

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.model.Progress
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.progress.model.ProgressResponse
import org.stepik.android.remote.progress.service.ProgressService
import javax.inject.Inject

class ProgressRemoteDataSourceImpl
@Inject
constructor(
    private val progressService: ProgressService
) : ProgressRemoteDataSource {
    private val progressResponseMapper =
        Function<ProgressResponse, List<Progress>>(ProgressResponse::progresses)

    override fun getProgress(progressId: String): Single<Progress> =
        getProgresses(progressId)
            .map { it.first() }

    override fun getProgresses(vararg progressIds: String?): Single<List<Progress>> =
        progressIds
            .chunkedSingleMap { ids ->
                progressService.getProgressesReactive(ids)
                    .map(progressResponseMapper)
            }
}