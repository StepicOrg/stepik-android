package org.stepik.android.cache.visited_courses

import io.reactivex.Completable
import io.reactivex.Flowable
import org.stepik.android.cache.visited_courses.dao.VisitedCourseDao
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import javax.inject.Inject

class VisitedCoursesCacheDataSourceImpl
@Inject
constructor(
    private val visitedCourseDao: VisitedCourseDao
) : VisitedCoursesCacheDataSource {
    override fun getVisitedCourses(): Flowable<List<VisitedCourse>> =
        visitedCourseDao.getVisitedCourses()

    override fun saveVisitedCourses(visitedCourses: List<VisitedCourse>): Completable =
        visitedCourseDao.saveVisitedCourses(visitedCourses)

    override fun saveVisitedCourse(courseId: Long): Completable =
        Completable.fromCallable {
            visitedCourseDao
                .saveVisitedCourse(courseId)
        }
}