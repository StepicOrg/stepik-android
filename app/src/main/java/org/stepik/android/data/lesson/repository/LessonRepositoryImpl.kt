package org.stepik.android.data.lesson.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.requireSize
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
    override fun getLessons(vararg lessonIds: Long, primarySourceType: DataSourceType): Single<List<Lesson>> {
        val remoteSource = lessonRemoteDataSource
            .getLessons(*lessonIds)
            .doCompletableOnSuccess(lessonCacheDataSource::saveLessons)

        val cacheSource = lessonCacheDataSource
            .getLessons(*lessonIds)

        return when (primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource.requireSize(lessonIds.size))

            DataSourceType.CACHE ->
                cacheSource.flatMap { cachedLessons ->
                    val ids = (lessonIds.toList() - cachedLessons.map(Lesson::id)).toLongArray()
                    lessonRemoteDataSource
                        .getLessons(*ids)
                        .doCompletableOnSuccess(lessonCacheDataSource::saveLessons)
                        .map { remoteLessons -> cachedLessons + remoteLessons }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }.map { lessons -> lessons.sortedBy { lessonIds.indexOf(it.id) } }
    }
}