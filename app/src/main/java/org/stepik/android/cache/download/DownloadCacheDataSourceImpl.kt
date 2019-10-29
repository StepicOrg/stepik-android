package org.stepik.android.cache.download

import io.reactivex.Single
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentState
import org.stepik.android.data.download.source.DownloadCacheDataSource
import javax.inject.Inject

class DownloadCacheDataSourceImpl
@Inject
constructor(
    private val persistentStateDao: PersistentStateDao
) : DownloadCacheDataSource {
    override fun getDownloadedCoursesIds(): Single<List<Long>> =
        Single.fromCallable {
            persistentStateDao
                .getAll(DBStructurePersistentState.Columns.TYPE, PersistentState.Type.COURSE.name)
                .map { it.id }
        }
}