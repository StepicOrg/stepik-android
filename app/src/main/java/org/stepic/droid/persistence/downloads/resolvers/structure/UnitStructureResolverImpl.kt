package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.compose
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import javax.inject.Inject

@AppSingleton
class UnitStructureResolverImpl
@Inject
constructor(
        private val sectionRepository: Repository<Section>,
        private val unitRepository: Repository<Unit>,
        private val lessonRepository: Repository<Lesson>,

        private val stepStructureResolver: StepStructureResolver
): UnitStructureResolver {
    override fun resolveStructure(vararg ids: Long): Observable<Structure> =
            Observable.just(ids)
                    .map(List<Unit>::toTypedArray compose Iterable<Unit>::toList compose unitRepository::getObjects)
                    .flatMap(::resolveStructure)

    override fun resolveStructure(vararg items: Unit): Observable<Structure> =
            items.toObservable().flatMap { unit ->
                val section = sectionRepository.getObject(unit.section)!!
                convertToPersistentItems(section.course, section.id, unit)
            }

    override fun resolveStructure(courseId: Long, sectionId: Long, vararg unitIds: Long): Observable<Structure> =
            Observable.just(unitIds)
                    .map(unitRepository::getObjects)
                    .flatMap { convertToPersistentItems(courseId, sectionId, *it.toList().toTypedArray()) }

    private fun convertToPersistentItems(courseId: Long, sectionId: Long, vararg units: Unit): Observable<Structure> =
            units.toObservable().flatMap { unit ->
                val lesson = lessonRepository.getObject(unit.lesson)!!
                stepStructureResolver.resolveStructure(courseId, sectionId, unit.id, lesson.id, *lesson.steps)
            }
}