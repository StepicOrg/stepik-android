package org.stepik.android.data.unit.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Unit

interface UnitCacheDataSource {
    fun getUnits(unitIds: List<Long>): Single<List<Unit>>

    fun getUnitsByLessonId(lessonId: Long): Single<List<Unit>>

    fun saveUnits(units: List<Unit>): Completable
}