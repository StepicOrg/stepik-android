package org.stepik.android.data.personal_deadlines.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.personal_deadlines.source.DeadlinesBannerCacheDataSource
import org.stepik.android.domain.personal_deadlines.repository.DeadlinesBannerRepository
import javax.inject.Inject

class DeadlinesBannerRepositoryImpl
@Inject
constructor(
    private val deadlinesBannerCacheDataSource: DeadlinesBannerCacheDataSource
) : DeadlinesBannerRepository {
    override fun addCourseId(courseId: Long): Completable =
        deadlinesBannerCacheDataSource.addCourseId(courseId)

    override fun removeCourseId(courseId: Long): Completable =
        deadlinesBannerCacheDataSource.removeCourseId(courseId)

    override fun hasCourseId(courseId: Long): Single<Boolean> =
        deadlinesBannerCacheDataSource.hasCourseId(courseId)
}