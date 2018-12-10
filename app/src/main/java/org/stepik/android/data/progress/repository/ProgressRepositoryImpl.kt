package org.stepik.android.data.progress.repository

import io.reactivex.Single
import org.stepic.droid.util.doOnSuccess
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Progress
import javax.inject.Inject

class ProgressRepositoryImpl
@Inject
constructor(
    private val progressRemoteDataSource: ProgressRemoteDataSource,
    private val progressCacheDataSource: ProgressCacheDataSource
) : ProgressRepository {
    override fun getProgress(progressId: String): Single<Progress> =
        progressRemoteDataSource
            .getProgress(progressId)
            .doOnSuccess(progressCacheDataSource::saveProgress)
            .onErrorResumeNext(progressCacheDataSource.getProgress(progressId).toSingle())

    override fun getProgresses(vararg progressIds: String): Single<List<Progress>> =
        progressRemoteDataSource
            .getProgresses(*progressIds)
            .doOnSuccess(progressCacheDataSource::saveProgresses)
            .onErrorResumeNext(progressCacheDataSource.getProgresses(*progressIds))
}