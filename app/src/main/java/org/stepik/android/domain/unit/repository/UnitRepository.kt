package org.stepik.android.domain.unit.repository

import io.reactivex.Maybe
import org.stepik.android.model.Unit

interface UnitRepository {
    fun getUnit(unitId: Long): Maybe<Unit>
}