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
        val remote = mobileTiersRemoteDataSource
            .getMobileTiers(mobileTierCalculations)
            .doCompletableOnSuccess(mobileTiersCacheDataSource::saveMobileTiers)

        val cache = mobileTiersCacheDataSource.getMobileTiers(mobileTierCalculations.map(MobileTierCalculation::course))

        return when (dataSourceType) {
            DataSourceType.REMOTE ->
                remote.onErrorResumeNext(cache)

            DataSourceType.CACHE ->
                cache
                    .filter(List<MobileTier>::isNotEmpty)
                    .switchIfEmpty(remote)

            else -> throw IllegalArgumentException("Unsupported source type = $dataSourceType")
        }
    }
}