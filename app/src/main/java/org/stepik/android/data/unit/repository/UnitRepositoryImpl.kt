package org.stepik.android.data.unit.repository

import io.reactivex.Maybe
import org.stepic.droid.util.doOnSuccess
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitRepositoryImpl
@Inject
constructor(
    private val unitCacheDataSource: UnitCacheDataSource,
    private val unitRemoteDataSource: UnitRemoteDataSource
) : UnitRepository {

    override fun getUnit(unitId: Long, primarySourceType: DataSourceType): Maybe<Unit> {
        val remoteSource = unitRemoteDataSource
            .getUnit(unitId)
            .doOnSuccess(unitCacheDataSource::saveUnit)

        val cacheSource = unitCacheDataSource
            .getUnit(unitId)

        return when(primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource.switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

}