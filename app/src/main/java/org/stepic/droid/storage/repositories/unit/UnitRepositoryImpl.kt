package org.stepic.droid.storage.repositories.unit

import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.model.Unit
import javax.inject.Inject

class UnitRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val unitRemoteDataSource: UnitRemoteDataSource
) : Repository<Unit> {

    override fun getObjects(keys: LongArray): Iterable<Unit> {
        val keyList = keys.toList()
        var units = databaseFacade.getUnitsByIds(keyList)
        if (units.size != keys.size) {
            units =
                    try {
                        unitRemoteDataSource.getUnitsByCourseAndLessonId(*keys).blockingGet()?.also {
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
                        unitRemoteDataSource.getUnitsByCourseAndLessonId(key).blockingGet()
                                ?.firstOrNull()
                                ?.also(databaseFacade::addUnit)
                    } catch (exception: Exception) {
                        null
                    }
        }
        return unit
    }

}

