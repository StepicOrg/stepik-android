package org.stepik.android.data.lesson.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.lesson.source.LessonCacheDataSource
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.model.Lesson
import javax.inject.Inject

class LessonRepositoryImpl
@Inject
constructor(
    private val lessonRemoteDataSource: LessonRemoteDataSource,
    private val lessonCacheDataSource: LessonCacheDataSource
) : LessonRepository {
    private val delegate =
        ListRepositoryDelegate(
            lessonRemoteDataSource::getLessons,
            lessonCacheDataSource::getLessons,
            lessonCacheDataSource::saveLessons
        )

    override fun getLessons(vararg lessonIds: Long, primarySourceType: DataSourceType): Single<List<Lesson>> =
        delegate.get(lessonIds.toList(), primarySourceType, allowFallback = true)

    override fun removeCachedLessons(courseId: Long): Completable =
        lessonCacheDataSource.removeCachedLessons(courseId)
}