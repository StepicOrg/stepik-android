package org.stepik.android.domain.unit.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Unit

interface UnitRepository {
    fun getUnit(unitId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Unit>
    fun getUnits(vararg unitIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Unit>>
}