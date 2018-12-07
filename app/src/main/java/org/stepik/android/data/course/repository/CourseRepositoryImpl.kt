package org.stepik.android.data.course.repository

import io.reactivex.Maybe
import org.stepic.droid.util.doOnSuccess
import org.stepic.droid.util.maybeFirst
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseRepositoryImpl
@Inject
constructor(
        private val courseRemoteDataSource: CourseRemoteDataSource,
        private val courseCacheDataSource: CourseCacheDataSource
) : CourseRepository {

    override fun getCourse(courseId: Long, canUseCache: Boolean): Maybe<Course> {
        val remoteSource = courseRemoteDataSource.getCourses(courseId).maybeFirst()
            .doOnSuccess(courseCacheDataSource::saveCourse)

        val cacheSource = courseCacheDataSource.getCourses(courseId).maybeFirst()

        return if (canUseCache) {
            cacheSource.switchIfEmpty(remoteSource)
        } else {
            remoteSource
        }
    }

}