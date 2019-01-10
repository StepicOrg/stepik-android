package org.stepik.android.data.unit.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
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
                remoteSource.onErrorResumeNext(cacheSource.requireSize(unitIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedUnits ->
                    val ids = (unitIds.toList() - cachedUnits.map(Unit::id)).toLongArray()
                    unitRemoteDataSource
                        .getUnits(*ids)
                        .doCompletableOnSuccess(unitCacheDataSource::saveUnits)
                        .map { remoteUnits -> cachedUnits + remoteUnits }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { units -> units.sortedBy { unitIds.indexOf(it.id) } }
    }

    override fun getUnitsByLessonId(lessonId: Long, primarySourceType: DataSourceType): Single<List<Unit>> {
        val remoteSource = unitRemoteDataSource
            .getUnitsByLessonId(lessonId)
            .doCompletableOnSuccess(unitCacheDataSource::saveUnits)

        val cacheSource = unitCacheDataSource
            .getUnitsByLessonId(lessonId)

        return when(primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource
                    .filter(List<Unit>::isNotEmpty)
                    .switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }
}