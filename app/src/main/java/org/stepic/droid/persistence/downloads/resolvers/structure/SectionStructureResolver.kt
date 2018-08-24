package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.storage.repositories.progress.ProgressRepository
import org.stepic.droid.util.then
import org.stepik.android.model.Section
import javax.inject.Inject

@AppSingleton
class SectionStructureResolver
@Inject
constructor(
        private val sectionRepository: Repository<Section>,
        private val unitPersistentAdapter: UnitStructureResolver,
        private val progressRepository: ProgressRepository
): StructureResolver<Section> {
    override fun resolveStructure(vararg ids: Long): Observable<Structure> =
            Observable.just(ids)
                    .flatMap { resolveStructure(*sectionRepository.getObjects(it).toList().toTypedArray()) }

    override fun resolveStructure(vararg items: Section): Observable<Structure> =
            progressRepository.syncProgresses(*items) then
            items.toObservable().flatMap { section ->
                unitPersistentAdapter.resolveStructure(section.course, section.id, unitIds = *section.units.toLongArray())
            }
}