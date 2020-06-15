package org.stepik.android.data.unit.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.model.Unit

interface UnitCacheDataSource {
    fun getUnit(unitId: Long): Maybe<Unit> =
        getUnits(unitId).maybeFirst()

    fun getUnits(vararg unitIds: Long): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>>

    fun saveUnit(unit: Unit): Completable =
        saveUnits(listOf(unit))

    fun saveUnits(units: List<Unit>): Completable
}