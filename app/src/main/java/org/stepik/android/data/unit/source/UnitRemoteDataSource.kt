package org.stepik.android.data.unit.source

import io.reactivex.Single
import org.stepik.android.model.Unit

interface UnitRemoteDataSource {
    fun getUnits(unitIds: List<Long>): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>>

    fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>>
}