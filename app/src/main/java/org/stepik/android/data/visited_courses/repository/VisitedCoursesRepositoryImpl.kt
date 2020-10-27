package org.stepik.android.data.visited_courses.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.data.visited_courses.source.VisitedCoursesRemoteDataSource
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import javax.inject.Inject

class VisitedCoursesRepositoryImpl
@Inject
constructor(
    private val visitedCoursesCacheDataSource: VisitedCoursesCacheDataSource,
    private val visitedCoursesRemoteDataSource: VisitedCoursesRemoteDataSource
) : VisitedCoursesRepository {

    private val behaviorRelay: BehaviorRelay<List<VisitedCourse>> = BehaviorRelay.create()

    private val visitedCoursesSource: Observable<List<VisitedCourse>> =
        visitedCoursesRemoteDataSource
            .getVisitedCourses()
            .toObservable()
            .filter { it.isNotEmpty() }
            .doOnNext(visitedCoursesCacheDataSource::saveVisitedCourses)
            .share()

    override fun observeVisitedCourses(): Observable<List<VisitedCourse>> =
        visitedCoursesCacheDataSource
            .getVisitedCourses()
            .toObservable()
            .filter { it.isNotEmpty() }
            .concatWith(visitedCoursesSource)
            .concatWith(behaviorRelay)

    override fun saveVisitedCourse(courseId: Long): Completable =
        visitedCoursesCacheDataSource
            .saveVisitedCourse(courseId)
}