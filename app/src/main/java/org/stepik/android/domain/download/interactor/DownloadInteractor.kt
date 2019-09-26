package org.stepik.android.domain.download.interactor

import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.download.repository.DownloadRepository
import javax.inject.Inject

class DownloadInteractor
@Inject
constructor(
    private val downloadRepository: DownloadRepository,
    private val courseRepository: CourseRepository
) {
}