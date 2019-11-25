package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import org.stepic.droid.persistence.model.Structure

interface StepStructureResolver {
    fun resolveStructure(
        courseId: Long,
        sectionId: Long,
        unitId: Long,
        lessonId: Long,
        vararg stepIds: Long,
        resolveNestedObjects: Boolean
    ): Observable<Structure>
}