package org.stepik.android.cache.visited_courses

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.visited_courses.dao.VisitedCourseDao
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import javax.inject.Inject

class VisitedCoursesCacheDataSourceImpl
@Inject
constructor(
    private val visitedCourseDao: VisitedCourseDao
) : VisitedCoursesCacheDataSource {
    override fun getVisitedCourses(): Single<List<VisitedCourse>> =
        Single
            .fromCallable {
                visitedCourseDao
                    .getVisitedCourses()
            }

    override fun saveVisitedCourse(visitedCourses: List<VisitedCourse>): Completable =
        Completable
            .fromCallable {
                visitedCourseDao
                    .saveVisitedCourse(visitedCourses)
            }
}