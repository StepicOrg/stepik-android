package org.stepik.android.data.lesson.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.model.Lesson

interface LessonRemoteDataSource {
    fun getLesson(lessonId: Long): Maybe<Lesson>
    fun getLessons(vararg lessonIds: Long): Single<List<Lesson>>
}