package org.stepik.android.cache.unit

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : UnitCacheDataSource {
    override fun getUnits(vararg unitIds: Long): Single<List<Unit>> =
        Single.fromCallable {
            databaseFacade.getUnitsByIds(unitIds.toList())
        }

    override fun saveUnits(units: List<Unit>): Completable =
        Completable.fromAction {
            databaseFacade.addUnits(units)
        }
}