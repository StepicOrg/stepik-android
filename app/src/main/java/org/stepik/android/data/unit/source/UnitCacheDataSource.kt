package org.stepik.android.data.unit.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Unit

interface UnitCacheDataSource {
    fun getUnit(unitId: Long): Maybe<Unit>
    fun getUnits(vararg unitIds: Long): Single<List<Unit>>

    fun saveUnit(unit: Unit): Completable
    fun saveUnits(units: List<Unit>): Completable
}