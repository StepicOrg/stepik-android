package org.stepik.android.data.lesson.source

import io.reactivex.Single
import org.stepik.android.model.Lesson

interface LessonRemoteDataSource {
    fun getLessons(lessonIds: List<Long>): Single<List<Lesson>>
}