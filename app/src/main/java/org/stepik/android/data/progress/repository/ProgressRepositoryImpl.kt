package org.stepik.android.data.progress.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
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
            .doCompletableOnSuccess(progressCacheDataSource::saveProgress)
            .onErrorResumeNext(progressCacheDataSource.getProgress(progressId).toSingle())

    override fun getProgresses(vararg progressIds: String,  primarySourceType: DataSourceType): Single<List<Progress>> {
        val remoteSource = progressRemoteDataSource
            .getProgresses(*progressIds)
            .doCompletableOnSuccess(progressCacheDataSource::saveProgresses)

        val cacheSource = progressCacheDataSource
            .getProgresses(*progressIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(progressIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedProgresses ->
                    val ids = (progressIds.toList() - cachedProgresses.mapNotNull(Progress::id)).toTypedArray()
                    progressRemoteDataSource
                        .getProgresses(*ids)
                        .doCompletableOnSuccess(progressCacheDataSource::saveProgresses)
                        .map { remoteProgresses -> cachedProgresses + remoteProgresses }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { progresses -> progresses.sortedBy { progressIds.indexOf(it.id) } }
    }

    override fun saveProgresses(progresses: List<Progress>): Completable =
        progressCacheDataSource
            .saveProgresses(progresses)
}