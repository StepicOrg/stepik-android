package org.stepic.droid.persistence.storage

import io.reactivex.Observer
import org.stepic.droid.persistence.di.PersistenceScope
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentState
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

@PersistenceScope
class PersistentStateManagerImpl
@Inject
constructor(
        private val persistentStateDao: PersistentStateDao,
        private val databaseFacade: DatabaseFacade,
        private val updatesObserver: Observer<Structure>
): PersistentStateManager {
    companion object {
        private fun concatStates(stateA: PersistentState.State, stateB: PersistentState.State) = when {
            stateA == PersistentState.State.IN_PROGRESS || stateB == PersistentState.State.IN_PROGRESS ->
                PersistentState.State.IN_PROGRESS

            stateA == PersistentState.State.NOT_CACHED || stateB == PersistentState.State.NOT_CACHED ->
                PersistentState.State.NOT_CACHED

            else ->
                PersistentState.State.CACHED
        }
    }

    private val lock = ReentrantReadWriteLock()

    override fun invalidateStructure(structure: Structure, state: PersistentState.State) = lock.write {
        persistentStateDao.insertOrReplace(PersistentState(structure.step, PersistentState.Type.STEP, state))
        invalidate(structure.lesson, PersistentState.Type.LESSON)
        invalidate(structure.unit, PersistentState.Type.UNIT)
        invalidate(structure.section, PersistentState.Type.SECTION)
        invalidate(structure.course, PersistentState.Type.COURSE)
        updatesObserver.onNext(structure)
    }

    private fun invalidate(id: Long, type: PersistentState.Type) {
        when (type) {
            PersistentState.Type.STEP -> {}

            PersistentState.Type.LESSON -> {
                val lesson = databaseFacade.getLessonById(id) ?: return

                val state = lesson.steps.map { getState(it, PersistentState.Type.STEP) }.reduce(::concatStates)
                persistentStateDao.insertOrReplace(PersistentState(lesson.id, PersistentState.Type.LESSON, state))
            }

            PersistentState.Type.UNIT -> {
                val unit = databaseFacade.getUnitById(id) ?: return

                val state = getState(unit.lesson, PersistentState.Type.LESSON)
                persistentStateDao.insertOrReplace(PersistentState(unit.id, PersistentState.Type.UNIT, state))
            }

            PersistentState.Type.SECTION -> {
                val section = databaseFacade.getSectionById(id) ?: return

                val state = section.units.map { getState(it, PersistentState.Type.UNIT) }.reduce(::concatStates)
                persistentStateDao.insertOrReplace(PersistentState(section.id, PersistentState.Type.SECTION, state))
            }

            PersistentState.Type.COURSE -> {
                val course = databaseFacade.getCourseById(id, Table.enrolled) ?: return
                val sections = course.sections ?: return

                val state = sections.map { getState(it, PersistentState.Type.SECTION) }.reduce(::concatStates)
                persistentStateDao.insertOrReplace(PersistentState(course.id, PersistentState.Type.COURSE, state))
            }
        }
    }

    private fun getStateLockFree(id: Long, type: PersistentState.Type): PersistentState.State =
            persistentStateDao.get(mapOf(DBStructurePersistentState.Columns.ID to id.toString(), DBStructurePersistentState.Columns.TYPE to type.name))
                    ?.state ?: PersistentState.State.NOT_CACHED

    override fun getState(id: Long, type: PersistentState.Type): PersistentState.State =
            lock.read { getStateLockFree(id, type) }

    override fun resetInProgressItems() = lock.write {
        persistentStateDao.resetInProgressItems()
    }
}