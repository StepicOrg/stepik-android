package org.stepik.android.data.mobile_tiers.repository

import io.reactivex.Single
import org.stepik.android.data.mobile_tiers.source.MobileTiersCacheDataSource
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class MobileTiersRepositoryImpl
@Inject
constructor(
    private val mobileTiersRemoteDataSource: MobileTiersRemoteDataSource,
    private val mobileTiersCacheDataSource: MobileTiersCacheDataSource
) : MobileTiersRepository {
    override fun calculateMobileTiers(mobileTierCalculations: List<MobileTierCalculation>, dataSourceType: DataSourceType): Single<List<MobileTier>> {
        if (mobileTierCalculations.isEmpty()) {
            return Single.just(emptyList())
        }

        val remote = remoteAction(mobileTierCalculations)

        val cache = mobileTiersCacheDataSource.getMobileTiers(mobileTierCalculations.map(MobileTierCalculation::course))

        return when (dataSourceType) {
            DataSourceType.REMOTE ->
                remote.onErrorResumeNext(cache)

            DataSourceType.CACHE ->
                cache.flatMap { cachedItems ->
                    val newIds = (mobileTierCalculations.map { it.course } - cachedItems.map { it.course })
                    val newMobileCalculations = newIds.map { MobileTierCalculation(course = it) }
                    remoteAction(newMobileCalculations)
                            .map { remoteItems -> (cachedItems + remoteItems) }
                }

            else -> throw IllegalArgumentException("Unsupported source type = $dataSourceType")
        }
    }
    private fun remoteAction(mobileTierCalculations: List<MobileTierCalculation>): Single<List<MobileTier>> =
        mobileTiersRemoteDataSource
            .getMobileTiers(mobileTierCalculations)
            .doCompletableOnSuccess(mobileTiersCacheDataSource::saveMobileTiers)
}