package org.stepik.android.cache.unit

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.maybeFirst
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : UnitCacheDataSource {
    override fun getUnit(unitId: Long): Maybe<Unit> =
        getUnits(unitId)
            .maybeFirst()

    override fun getUnits(vararg unitIds: Long): Single<List<Unit>> =
        Single.fromCallable {
            databaseFacade.getUnitsByIds(unitIds.toList())
        }

    override fun saveUnit(unit: Unit): Completable =
        saveUnits(listOf(unit))

    override fun saveUnits(units: List<Unit>): Completable =
        Completable.fromAction {
            databaseFacade.addUnits(units)
        }
}