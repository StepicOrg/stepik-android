package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.assignment.repository.AssignmentRepository
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Unit
import javax.inject.Inject

@AppSingleton
class UnitStructureResolverImpl
@Inject
constructor(
    private val sectionRepository: SectionRepository,
    private val unitRepository: UnitRepository,
    private val lessonRepository: LessonRepository,

    private val assignmentRepository: AssignmentRepository,
    private val progressRepository: ProgressRepository,

    private val stepStructureResolver: StepStructureResolver
): UnitStructureResolver {
    override fun resolveStructure(vararg ids: Long, resolveNestedObjects: Boolean): Observable<Structure> =
        unitRepository
            .getUnits(*ids)
            .flatMapObservable { units ->
                resolveStructure(*units.toTypedArray(), resolveNestedObjects = resolveNestedObjects)
            }

    override fun resolveStructure(vararg items: Unit, resolveNestedObjects: Boolean): Observable<Structure> =
        sectionRepository
            .getSections(*items.mapToLongArray(Unit::section))
            .flatMapObservable { sections ->
                val observables =
                    items.mapNotNull { unit ->
                        sections
                            .find { it.id == unit.section }
                            ?.let { resolveStructure(it.course, it.id, unit, resolveNestedObjects = resolveNestedObjects) }
                    }
                Observable.concat(observables)
            }

    override fun resolveStructure(courseId: Long, sectionId: Long, vararg unitIds: Long, resolveNestedObjects: Boolean): Observable<Structure> =
        unitRepository
            .getUnits(*unitIds)
            .flatMapObservable { units ->
                resolveStructure(courseId, sectionId, *units.toTypedArray(), resolveNestedObjects = resolveNestedObjects)
            }

    private fun resolveStructure(courseId: Long, sectionId: Long, vararg units: Unit, resolveNestedObjects: Boolean): Observable<Structure> =
        lessonRepository
            .getLessons(*units.mapToLongArray(Unit::lesson))
            .flatMapObservable { lessons ->
                val assignmentIds = units.mapNotNull(Unit::assignments).fold(longArrayOf(), LongArray::plus)
                val progresses = units.asIterable().getProgresses() + lessons.getProgresses()

                val observables =
                    units.mapNotNull { unit ->
                        lessons
                            .find { it.id == unit.lesson }
                            ?.let { stepStructureResolver.resolveStructure(courseId, sectionId, unit.id, it.id, *it.steps, resolveNestedObjects = resolveNestedObjects) }
                    }

                val nestedObjectsCompletableSource =
                    if (resolveNestedObjects) {
                        assignmentRepository
                            .getAssignments(*assignmentIds, primarySourceType = DataSourceType.REMOTE)
                            .ignoreElement()
                            .andThen(progressRepository.getProgresses(*progresses))
                            .ignoreElement()
                    } else {
                        Completable.complete()
                    }

                nestedObjectsCompletableSource.andThen(Observable.concat(observables))
            }
}