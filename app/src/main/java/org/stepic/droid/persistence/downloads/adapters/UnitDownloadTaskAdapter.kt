package org.stepic.droid.persistence.downloads.adapters

import io.reactivex.Completable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.downloads.DownloadTaskManager
import org.stepic.droid.persistence.model.DownloadConfiguration
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.storage.repositories.Repository
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import javax.inject.Inject

@AppSingleton
class UnitDownloadTaskAdapter
@Inject
constructor(
        private val sectionRepository: Repository<Section>,
        private val unitRepository: Repository<Unit>,
        private val lessonRepository: Repository<Lesson>,

        private val stepDownloadTaskAdapter: StepDownloadTaskAdapter,

        persistentItemDao: PersistentItemDao,
        downloadTaskManager: DownloadTaskManager
): DownloadTaskAdapterBase(persistentItemDao, downloadTaskManager) {
    override fun createTask(vararg ids: Long, configuration: DownloadConfiguration): Completable =
            ids.toObservable().map(unitRepository::getObject).flatMapCompletable { unit ->
                val section = sectionRepository.getObject(unit.section)!!
                val courseId = section.course

                addTask(courseId, section.id, unit, configuration)
            }

    fun addTask(courseId: Long, sectionId: Long, vararg unitIds: Long, configuration: DownloadConfiguration): Completable =
            unitIds.toObservable().map(unitRepository::getObject).flatMapCompletable { unit ->
                addTask(courseId, sectionId, unit, configuration)
            }

    private fun addTask(courseId: Long, sectionId: Long, unit: Unit, configuration: DownloadConfiguration): Completable {
        val lesson = lessonRepository.getObject(unit.lesson)!!
        return stepDownloadTaskAdapter.addTask(courseId, sectionId, unit.id, lesson.id, *lesson.steps, configuration = configuration)
    }

    override val persistentItemKeyFieldColumn: String = DBStructurePersistentItem.Columns.UNIT
}