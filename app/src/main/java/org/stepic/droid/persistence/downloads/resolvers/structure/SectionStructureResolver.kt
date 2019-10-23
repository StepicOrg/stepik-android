package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
import org.stepic.droid.util.then
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.model.Section
import javax.inject.Inject

@AppSingleton
class SectionStructureResolver
@Inject
constructor(
    private val sectionRepository: SectionRepository,
    private val unitStructureResolver: UnitStructureResolver,
    private val progressRepository: ProgressRepository
): StructureResolver<Section> {
    override fun resolveStructure(vararg ids: Long): Observable<Structure> =
        sectionRepository
            .getSections(*ids)
            .flatMapObservable { sections ->
                resolveStructure(*sections.toTypedArray())
            }

    override fun resolveStructure(vararg items: Section): Observable<Structure> =
        progressRepository.getProgresses(*items.asIterable().getProgresses()).ignoreElement() then
                Observable.concat(
                    items.map { section ->
                        unitStructureResolver.resolveStructure(section.course, section.id, unitIds = *section.units.toLongArray())
                    }
                )
}