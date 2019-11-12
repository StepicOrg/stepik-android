package org.stepik.android.remote.unit

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.model.Unit
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.unit.model.UnitResponse
import org.stepik.android.remote.unit.service.UnitService
import javax.inject.Inject

class UnitRemoteDataSourceImpl
@Inject
constructor(
    private val unitService: UnitService
) : UnitRemoteDataSource {
    private val unitResponseMapper =
        Function<UnitResponse, List<Unit>>(UnitResponse::units)

    override fun getUnitsByCourseAndLessonId(vararg unitIds: Long): Single<List<Unit>> =
        unitIds
            .chunkedSingleMap { ids ->
                unitService.getUnitsRx(ids)
                    .map(unitResponseMapper)
            }

    override fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>> =
        unitService.getUnitsByLessonId(lessonId)
            .map(unitResponseMapper)

    override fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>> =
        unitService.getUnits(courseId, lessonId).map(unitResponseMapper)
}