package org.stepik.android.remote.unit

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepic.droid.web.UnitMetaResponse
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : UnitRemoteDataSource {
    override fun getUnits(vararg unitIds: Long): Single<List<Unit>> =
        if (unitIds.isEmpty()) {
            Single.just(emptyList())
        } else {
            api.getUnitsRx(unitIds)
                .map(UnitMetaResponse::units)
        }
}