package org.stepik.android.domain.unit.repository

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Unit

interface UnitRepository {
    fun getUnit(unitId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Unit> =
        getUnits(listOf(unitId), primarySourceType = primarySourceType).maybeFirst()

    fun getUnits(unitIds: List<Long>, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Unit>>

    fun getUnitsByCourseAndLessonId(courseId: Long, lessonId: Long): Single<List<Unit>>
}