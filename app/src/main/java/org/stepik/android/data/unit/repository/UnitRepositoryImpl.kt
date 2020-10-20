package org.stepik.android.data.unit.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Unit
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class UnitRepositoryImpl
@Inject
constructor(
    private val unitCacheDataSource: UnitCacheDataSource,
    private val unitRemoteDataSource: UnitRemoteDataSource
) : UnitRepository {
    private val delegate =
        ListRepositoryDelegate(
            unitRemoteDataSource::getUnits,
            unitCacheDataSource::getUnits,
            unitCacheDataSource::saveUnits
        )

    override fun getUnits(unitIds: List<Long>, primarySourceType: DataSourceType): Single<List<Unit>> =
        delegate.get(unitIds, primarySourceType, allowFallback = true)

    override fun getUnitsByLessonId(lessonId: Long, primarySourceType: DataSourceType): Single<List<Unit>> {
        val remoteSource = unitRemoteDataSource
            .getUnitsByLessonId(lessonId)
            .doCompletableOnSuccess(unitCacheDataSource::saveUnits)

        val cacheSource = unitCacheDataSource
            .getUnitsByLessonId(lessonId)

        return when (primarySourceType) {
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

    override fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>> =
        unitRemoteDataSource.getUnitsByCourseAndLessonId(courseId, lessonId)
}