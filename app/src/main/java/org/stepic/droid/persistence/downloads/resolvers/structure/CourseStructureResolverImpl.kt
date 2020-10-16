package org.stepic.droid.persistence.downloads.resolvers.structure

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.persistence.model.Structure
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.model.Course
import javax.inject.Inject

@AppSingleton
class CourseStructureResolverImpl
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val sectionStructureResolver: SectionStructureResolver,
    private val progressRepository: ProgressRepository
): StructureResolver<Course> {
    override fun resolveStructure(vararg ids: Long, resolveNestedObjects: Boolean): Observable<Structure> =
        courseRepository
            .getCourses(*ids)
            .flatMapObservable { courses ->
                resolveStructure(*courses.toTypedArray(), resolveNestedObjects = resolveNestedObjects)
            }

    override fun resolveStructure(vararg items: Course, resolveNestedObjects: Boolean): Observable<Structure> =
        if (resolveNestedObjects) {
            progressRepository
                .getProgresses(items.asIterable().getProgresses())
                .ignoreElement()
        } else {
            Completable.complete()
        }
            .andThen(
                items
                    .map { sectionStructureResolver.resolveStructure(*it.sections?.toLongArray() ?: longArrayOf(), resolveNestedObjects = resolveNestedObjects) }
                    .let { Observable.concat(it) }
            )
}