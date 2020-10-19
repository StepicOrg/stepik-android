package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Observable
import org.stepic.droid.persistence.model.Structure

interface StructureResolver<T> {
    fun resolveStructure(ids: List<Long>, resolveNestedObjects: Boolean): Observable<Structure>
    fun resolveStructure(vararg items: T, resolveNestedObjects: Boolean): Observable<Structure>
}