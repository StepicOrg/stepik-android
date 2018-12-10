package org.stepik.android.remote.lesson

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepic.droid.web.LessonStepicResponse
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.model.Lesson
import javax.inject.Inject

class LessonRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : LessonRemoteDataSource {
    override fun getLessons(vararg lessonIds: Long): Single<List<Lesson>> =
        api.getLessonsRx(lessonIds)
            .map(LessonStepicResponse::lessons)
}