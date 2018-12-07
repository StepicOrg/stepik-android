package org.stepik.android.cache.lesson

import io.reactivex.Completable
import io.reactivex.Maybe
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
    override fun getLesson(lessonId: Long): Maybe<Lesson> =
        Maybe.create { emitter ->
            databaseFacade
                .getLessonById(lessonId)
                ?.let(emitter::onSuccess)
                ?: emitter.onComplete()
        }

    override fun getLessons(vararg lessonIds: Long): Single<List<Lesson>> =
        Single.fromCallable {
            databaseFacade.getLessonsByIds(lessonIds)
        }

    override fun saveLesson(lesson: Lesson): Completable =
        Completable.fromAction {
            databaseFacade.addLesson(lesson)
        }
}