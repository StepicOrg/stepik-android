package org.stepik.android.data.unit.source

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.model.Unit

interface UnitRemoteDataSource {
    fun getUnit(unitId: Long): Maybe<Unit> =
        getUnits(listOf(unitId)).maybeFirst()

    fun getUnits(unitIds: List<Long>): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>>

    fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>>
}