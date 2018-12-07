package org.stepik.android.data.lesson.repository

import io.reactivex.Maybe
import org.stepic.droid.util.doOnSuccess
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

    override fun getLesson(lessonId: Long, primarySourceType: DataSourceType): Maybe<Lesson> {
        val remoteSource = lessonRemoteDataSource
            .getLesson(lessonId)
            .doOnSuccess(lessonCacheDataSource::saveLesson)

        val cacheSource = lessonCacheDataSource
            .getLesson(lessonId)

        return when(primarySourceType) {
            DataSourceType.REMOTE ->
                remoteSource.onErrorResumeNext(cacheSource)

            DataSourceType.CACHE ->
                cacheSource.switchIfEmpty(remoteSource)

            else ->
                throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }
    }

}