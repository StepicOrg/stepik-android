package org.stepik.android.remote.lesson

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.model.Lesson
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.lesson.model.LessonResponse
import javax.inject.Inject

class LessonRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : LessonRemoteDataSource {
    private val lessonResponseMapper =
        Function<LessonResponse, List<Lesson>>(LessonResponse::lessons)

    override fun getLessons(vararg lessonIds: Long): Single<List<Lesson>> =
        lessonIds
            .chunkedSingleMap { ids ->
                api.getLessonsRx(ids)
                    .map(lessonResponseMapper)
            }
}