package org.stepic.droid.storage.repositories.unit

import org.stepik.android.model.Unit
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class UnitRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Unit> {

    override fun getObjects(keys: LongArray): Iterable<Unit> {
        val keyList = keys.toList()
        var units = databaseFacade.getUnitsByIds(keyList)
        if (units.size != keys.size) {
            units =
                    try {
                        api.getUnits(keyList).execute()?.body()?.units?.also {
                            it.forEach(databaseFacade::addUnit)
                        } ?: emptyList()
                    } catch (exception: Exception) {
                        emptyList()
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
                        api.getUnits(listOf(key)).execute()
                                ?.body()
                                ?.units
                                ?.firstOrNull()
                                ?.also(databaseFacade::addUnit)
                    } catch (exception: Exception) {
                        null
                    }
        }
        return unit
    }

}

