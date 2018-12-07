package org.stepik.android.cache.unit

import io.reactivex.Completable
import io.reactivex.Maybe
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
    override fun getUnit(unitId: Long): Maybe<Unit> =
        Maybe.create { emitter ->
            databaseFacade.getUnitById(unitId)?.let(emitter::onSuccess) ?: emitter.onComplete()
        }

    override fun getUnits(vararg unitIds: Long): Single<List<Unit>> =
        Single.fromCallable {
            databaseFacade.getUnitsByIds(unitIds.toList())
        }
    override fun saveUnit(unit: Unit): Completable =
        Completable.fromAction {
            databaseFacade.addUnit(unit)
        }
}