package org.stepik.android.data.visited_courses.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.data.visited_courses.source.VisitedCoursesRemoteDataSource
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import javax.inject.Inject

class VisitedCoursesRepositoryImpl
@Inject
constructor(
    private val visitedCoursesCacheDataSource: VisitedCoursesCacheDataSource,
    visitedCoursesRemoteDataSource: VisitedCoursesRemoteDataSource
) : VisitedCoursesRepository {

    private val visitedCoursesSource: Completable =
        visitedCoursesRemoteDataSource
            .getVisitedCourses()
            .doOnSuccess(visitedCoursesCacheDataSource::saveVisitedCourses)
            .flatMapCompletable { Completable.complete().cache() } // cache only success

    override fun observeVisitedCourses(): Flowable<List<VisitedCourse>> =
        visitedCoursesSource
            .andThen(visitedCoursesCacheDataSource.getVisitedCourses())

    override fun saveVisitedCourse(courseId: Long): Completable =
        visitedCoursesCacheDataSource
            .saveVisitedCourse(courseId)
}