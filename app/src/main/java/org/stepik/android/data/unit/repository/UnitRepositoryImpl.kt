package org.stepik.android.data.unit.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
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
    override fun getUnits(vararg unitIds: Long, primarySourceType: DataSourceType): Single<List<Unit>> {
        val remoteSource = unitRemoteDataSource
            .getUnits(*unitIds)
            .doCompletableOnSuccess(unitCacheDataSource::saveUnits)

        val cacheSource = unitCacheDataSource
            .getUnits(*unitIds)

        return when(primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedSections ->
                    val ids = (unitIds.toList() - cachedSections.map(Unit::id)).toLongArray()
                    unitRemoteDataSource
                        .getUnits(*ids)
                        .doCompletableOnSuccess(unitCacheDataSource::saveUnits)
                        .map { remoteSections -> cachedSections + remoteSections }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { sections -> sections.sortedBy { unitIds.indexOf(it.id) } }
    }
}