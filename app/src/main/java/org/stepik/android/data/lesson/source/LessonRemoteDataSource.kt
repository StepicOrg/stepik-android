package org.stepik.android.data.lesson.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.model.Lesson
import org.stepik.android.remote.lesson.model.LessonResponse
import retrofit2.Call

interface LessonRemoteDataSource {
    fun getLessons(vararg lessonIds: Long): Call<LessonResponse>

    fun getLessonRx(lessonId: Long): Maybe<Lesson> =
        getLessonsRx(lessonId).maybeFirst()

    fun getLessonsRx(vararg lessonIds: Long): Single<List<Lesson>>
}