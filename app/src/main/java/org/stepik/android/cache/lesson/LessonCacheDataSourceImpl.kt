package org.stepik.android.cache.lesson

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.lesson.source.LessonCacheDataSource
import org.stepik.android.model.Lesson
import javax.inject.Inject

class LessonCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : LessonCacheDataSource {
    override fun getLessons(lessonIds: List<Long>): Single<List<Lesson>> =
        Single.fromCallable {
            databaseFacade.getLessonsByIds(lessonIds.toLongArray())
        }

    override fun saveLessons(lessons: List<Lesson>): Completable =
        Completable.fromAction {
            databaseFacade.addLessons(lessons)
        }

    override fun removeCachedLessons(courseId: Long): Completable =
        Completable.fromAction {
            databaseFacade.removeLessons(courseId)
        }
}