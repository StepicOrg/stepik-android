package org.stepik.android.domain.download.interactor

import io.reactivex.Single
import org.stepic.droid.persistence.downloads.progress.mapper.DownloadProgressStatusMapper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.repository.DownloadRepository
import org.stepik.android.model.Course
import org.stepik.android.view.injection.download.DownloadsProgressStatusMapper
import javax.inject.Inject

class DownloadInteractor
@Inject
constructor(
    private val downloadRepository: DownloadRepository,
    private val courseRepository: CourseRepository,
    @DownloadsProgressStatusMapper
    private val downloadStatusProgressStatusMapper: DownloadProgressStatusMapper
) {
    fun fetchDownloadedCoursesIds(): Single<List<Long>> =
        downloadRepository.getDownloadedCoursesIds()

    fun fetchDownloadCourses(): Single<List<Course>> =
        downloadRepository.getDownloadedCoursesIds()
            .flatMap { courseIds ->
                courseRepository.getCourses(*courseIds.toLongArray(), primarySourceType = DataSourceType.CACHE)
            }
}