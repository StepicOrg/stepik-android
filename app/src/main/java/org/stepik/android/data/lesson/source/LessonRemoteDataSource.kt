package org.stepik.android.data.lesson.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Lesson

interface LessonRemoteDataSource {
    fun getLesson(lessonId: Long): Maybe<Lesson> =
        getLessons(lessonId)
            .maybeFirst()

    fun getLessons(vararg lessonIds: Long): Single<List<Lesson>>
}