package org.stepik.android.domain.lesson.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Lesson

interface LessonRepository {
    fun getLesson(lessonId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Lesson> =
        getLessons(lessonId, primarySourceType = primarySourceType).maybeFirst()

    fun getLessons(vararg lessonIds: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<Lesson>>

    fun removeCachedLessons(courseId: Long): Completable
}