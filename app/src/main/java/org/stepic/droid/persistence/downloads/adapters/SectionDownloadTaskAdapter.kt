package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Section
import javax.inject.Inject

@AppSingleton
class SectionDownloadTaskAdapter
@Inject
constructor(
        private val sectionRepository: Repository<Section>,
        private val unitPersistentAdapter: UnitDownloadTaskAdapter
): DownloadTaskAdapter<Section> {
    override fun convertToTask(vararg ids: Long, configuration: DownloadConfiguration): Observable<DownloadTask> =
            Observable.just(ids)
                    .map(sectionRepository::getObjects)
                    .flatMap { convertToTask(*it.toList().toTypedArray(), configuration = configuration) }

    override fun convertToTask(vararg items: Section, configuration: DownloadConfiguration): Observable<DownloadTask> =
            items.toObservable().flatMap { section ->
                unitPersistentAdapter.convertToPersistentItems(section.course, section.id, unitIds = *section.units.toLongArray(), configuration = configuration)
            }
}