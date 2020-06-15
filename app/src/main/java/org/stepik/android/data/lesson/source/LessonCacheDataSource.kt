package org.stepik.android.data.lesson.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Lesson

interface LessonCacheDataSource {
    fun getLesson(lessonId: Long): Maybe<Lesson> =
        getLessons(lessonId).maybeFirst()

    fun getLessons(vararg lessonIds: Long): Single<List<Lesson>>

    fun saveLesson(lesson: Lesson): Completable =
        saveLessons(listOf(lesson))

    fun saveLessons(lessons: List<Lesson>): Completable

    fun removeCachedLessons(courseId: Long): Completable
}