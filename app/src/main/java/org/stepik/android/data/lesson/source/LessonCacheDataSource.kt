package org.stepik.android.data.lesson.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Lesson

interface LessonCacheDataSource {
    fun getLessons(lessonIds: List<Long>): Single<List<Lesson>>

    fun saveLessons(lessons: List<Lesson>): Completable

    fun removeCachedLessons(courseId: Long): Completable
}