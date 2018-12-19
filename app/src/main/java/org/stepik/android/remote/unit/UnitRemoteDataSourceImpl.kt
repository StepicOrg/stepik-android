package org.stepik.android.remote.unit

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepic.droid.web.UnitMetaResponse
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.model.Unit
import org.stepik.android.remote.base.chunkedSingleMap
import javax.inject.Inject

class UnitRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : UnitRemoteDataSource {
    private val unitResponseMapper =
        Function<UnitMetaResponse, List<Unit>>(UnitMetaResponse::units)

    override fun getUnits(vararg unitIds: Long): Single<List<Unit>> =
        unitIds
            .chunkedSingleMap { ids ->
                api.getUnitsRx(ids)
                    .map(unitResponseMapper)
            }
}