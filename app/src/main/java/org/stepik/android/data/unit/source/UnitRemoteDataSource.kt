package org.stepik.android.data.unit.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Unit

interface UnitRemoteDataSource {
    fun getUnit(unitId: Long): Maybe<Unit> =
        getUnits(unitId).maybeFirst()

    fun getUnits(vararg unitIds: Long): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>>

    fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>>
}