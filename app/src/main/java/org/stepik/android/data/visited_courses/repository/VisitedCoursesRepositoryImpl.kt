package org.stepik.android.data.visited_courses.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.di.AppSingleton
import org.stepik.android.data.visited_courses.source.VisitedCoursesCacheDataSource
import org.stepik.android.data.visited_courses.source.VisitedCoursesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import org.stepik.android.domain.visited_courses.repository.VisitedCoursesRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

@AppSingleton
class VisitedCoursesRepositoryImpl
@Inject
constructor(
    private val visitedCoursesCacheDataSource: VisitedCoursesCacheDataSource,
    private val visitedCoursesRemoteDataSource: VisitedCoursesRemoteDataSource
) : VisitedCoursesRepository {
    override fun getVisitedCourses(primarySourceType: DataSourceType): Single<List<VisitedCourse>> {
        val remoteSource = visitedCoursesRemoteDataSource
            .getVisitedCourses()
            .doCompletableOnSuccess(visitedCoursesCacheDataSource::saveVisitedCourses)

        val cacheSource = visitedCoursesCacheDataSource
            .getVisitedCourses()

        return when (primarySourceType) {
            DataSourceType.CACHE ->
                cacheSource
                    .filter(List<VisitedCourse>::isNotEmpty)
                    .switchIfEmpty(remoteSource)

            DataSourceType.REMOTE ->
                remoteSource

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

    override fun saveVisitedCourse(courseId: Long): Completable =
        visitedCoursesCacheDataSource
            .saveVisitedCourse(courseId)
}