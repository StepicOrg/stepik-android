package org.stepik.android.data.step.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
import org.stepik.android.data.step.source.StepCacheDataSource
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.model.Step
import javax.inject.Inject

class StepRepositoryImpl
@Inject
constructor(
    private val stepCacheDataSource: StepCacheDataSource,
    private val stepRemoteDataSource: StepRemoteDataSource
) : StepRepository {
    override fun getSteps(vararg stepIds: Long, primarySourceType: DataSourceType, cacheRemote: Boolean): Single<List<Step>> {
        val remoteSource = stepRemoteDataSource
            .getSteps(*stepIds)

        if (cacheRemote) {
             remoteSource.doCompletableOnSuccess(stepCacheDataSource::saveSteps)
        }

        val cacheSource = stepCacheDataSource
            .getSteps(*stepIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(stepIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedSteps ->
                    val ids = (stepIds.toList() - cachedSteps.map(Step::id)).toLongArray()
                    stepRemoteDataSource
                        .getSteps(*ids)
                        .doCompletableOnSuccess(stepCacheDataSource::saveSteps)
                        .map { remoteSteps -> cachedSteps + remoteSteps }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { steps -> steps.sortedBy { stepIds.indexOf(it.id) } }
    }
}