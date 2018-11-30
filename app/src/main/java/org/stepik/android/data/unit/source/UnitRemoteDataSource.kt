package org.stepik.android.data.unit.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Unit

interface UnitRemoteDataSource {
    fun getUnit(unitId: Long): Maybe<Unit>
    fun getUnits(vararg unitIds: Long): Single<List<Unit>>
}