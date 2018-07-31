package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Section
import javax.inject.Inject

@AppSingleton
class SectionDownloadTaskAdapter
@Inject
constructor(
        private val sectionRepository: Repository<Section>,

        private val unitDownloadTaskAdapter: UnitDownloadTaskAdapter,

        persistentItemDao: PersistentItemDao,
        downloadTaskManager: DownloadTaskManager
): DownloadTaskAdapterBase(persistentItemDao, downloadTaskManager) {
    override fun createTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            ids.toObservable().map(sectionRepository::getObject).flatMapCompletable { section ->
                unitDownloadTaskAdapter.addTask(section.course, section.id, unitIds = *section.units.toLongArray(), configuration = configuration)
            }

    override val persistentItemKeyFieldColumn: String = DBStructurePersistentItem.Columns.SECTION
}