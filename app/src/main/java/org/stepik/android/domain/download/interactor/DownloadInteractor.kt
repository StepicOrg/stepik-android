package org.stepik.android.domain.download.interactor

import org.stepic.droid.persistence.downloads.progress.CourseDownloadProgressProvider
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.repository.DownloadRepository

class DownloadInteractor(
    private val downloadRepository: DownloadRepository,
    private val courseRepository: CourseRepository,
    private val courseDownloadProgressProvider: CourseDownloadProgressProvider
) {
//    fun fetchDownloadedCoursesIds(): Single<List<Long>> =
//        downloadRepository.getDownloadedCoursesIds()
//
//    fun fetchDownloadCourses(): Single<List<Course>> =
//        downloadRepository.getDownloadedCoursesIds()
//            .flatMap { courseIds ->
//                courseRepository.getCourses(*courseIds.toLongArray(), primarySourceType = DataSourceType.CACHE)
//            }
}