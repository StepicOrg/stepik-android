package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
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
    override fun resolveStructure(vararg ids: Long, resolveNestedObjects: Boolean): Observable<Structure> =
        sectionRepository
            .getSections(*ids)
            .flatMapObservable { sections ->
                resolveStructure(*sections.toTypedArray(), resolveNestedObjects = resolveNestedObjects)
            }

    override fun resolveStructure(vararg items: Section, resolveNestedObjects: Boolean): Observable<Structure> =
        if (resolveNestedObjects) {
            progressRepository
                .getProgresses(items.asIterable().getProgresses())
                .ignoreElement()
        } else {
            Completable.complete()
        }
            .andThen(
                items
                    .map { unitStructureResolver.resolveStructure(it.course, it.id, unitIds = it.units.toLongArray(), resolveNestedObjects = resolveNestedObjects) }
                    .let { Observable.concat(it) }
            )
}