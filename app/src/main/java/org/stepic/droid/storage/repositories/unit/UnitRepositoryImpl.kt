package org.stepic.droid.storage.repositories.unit

import org.stepic.droid.model.Unit
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class UnitRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Unit, Long> {

    override fun getObjects(keys: Array<Long>): Iterable<Unit> {
        var units = databaseFacade.getUnitsByIds(keys.toLongArray())
        if (units.size != keys.size) {
            units =
                    try {
                        api.getUnits(keys.toLongArray()).execute()?.body()?.units ?: ArrayList<Unit>()
                    } catch (exception: Exception) {
                        ArrayList<Unit>()
                    }
        }
        units = units.sortedBy { it.position }
        return units
    }

    override fun getObject(key: Long): Unit? {
        var unit = databaseFacade.getUnitById(key)
        if (unit == null) {
            unit =
                    try {
                        api.getUnits(longArrayOf(key)).execute()?.body()?.units?.firstOrNull()
                    } catch (exception: Exception) {
                        null
                    }
        }
        return unit
    }

}

