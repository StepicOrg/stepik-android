package org.stepik.android.data.progress.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Progress
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ProgressRepositoryImpl
@Inject
constructor(
    private val progressRemoteDataSource: ProgressRemoteDataSource,
    private val progressCacheDataSource: ProgressCacheDataSource
) : ProgressRepository {
    private val delegate =
        ListRepositoryDelegate(
            progressRemoteDataSource::getProgresses,
            progressCacheDataSource::getProgresses,
            progressCacheDataSource::saveProgresses
        )

    override fun getProgress(progressId: String): Single<Progress> =
        progressRemoteDataSource
            .getProgress(progressId)
            .doCompletableOnSuccess(progressCacheDataSource::saveProgress)
            .onErrorResumeNext(progressCacheDataSource.getProgress(progressId).toSingle())

    override fun getProgresses(progressIds: List<String>, primarySourceType: DataSourceType): Single<List<Progress>> =
        delegate.get(progressIds, primarySourceType, allowFallback = true)

    override fun saveProgresses(progresses: List<Progress>): Completable =
        progressCacheDataSource
            .saveProgresses(progresses)
}