package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.model.DownloadTask
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import javax.inject.Inject

@AppSingleton
class UnitDownloadTaskAdapterImpl
@Inject
constructor(
        private val sectionRepository: Repository<Section>,
        private val unitRepository: Repository<Unit>,
        private val lessonRepository: Repository<Lesson>,

        private val stepDownloadTaskAdapter: StepDownloadTaskAdapter
): UnitDownloadTaskAdapter {
    override fun convertToTask(vararg ids: Long, configuration: DownloadConfiguration): Observable<DownloadTask> =
            Observable.just(ids)
                    .map(unitRepository::getObjects)
                    .flatMap { convertToTask(*it.toList().toTypedArray(), configuration = configuration) }

    override fun convertToTask(vararg items: Unit, configuration: DownloadConfiguration): Observable<DownloadTask> =
            items.toObservable().flatMap { unit ->
                val section = sectionRepository.getObject(unit.section)!!
                convertToPersistentItems(section.course, section.id, unit, configuration = configuration)
            }

    override fun convertToPersistentItems(courseId: Long, sectionId: Long, vararg unitIds: Long, configuration: DownloadConfiguration): Observable<DownloadTask> =
            Observable.just(unitIds)
                    .map(unitRepository::getObjects)
                    .flatMap { convertToPersistentItems(courseId, sectionId, *it.toList().toTypedArray(), configuration = configuration) }

    private fun convertToPersistentItems(courseId: Long, sectionId: Long, vararg units: Unit, configuration: DownloadConfiguration): Observable<DownloadTask> =
            units.toObservable().flatMap { unit ->
                val lesson = lessonRepository.getObject(unit.lesson)!!
                stepDownloadTaskAdapter.convertToTask(courseId, sectionId, unit.id, lesson.id, stepIds = *lesson.steps, configuration = configuration)
            }
}