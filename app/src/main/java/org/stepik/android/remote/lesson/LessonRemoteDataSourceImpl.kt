package org.stepik.android.remote.lesson

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.lesson.source.LessonRemoteDataSource
import org.stepik.android.model.Lesson
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.lesson.model.LessonResponse
import org.stepik.android.remote.lesson.service.LessonService
import javax.inject.Inject

class LessonRemoteDataSourceImpl
@Inject
constructor(
    private val lessonService: LessonService
) : LessonRemoteDataSource {
    private val lessonResponseMapper =
        Function<LessonResponse, List<Lesson>>(LessonResponse::lessons)

    override fun getLessons(lessonIds: List<Long>): Single<List<Lesson>> =
        lessonIds
            .chunkedSingleMap { ids ->
                lessonService.getLessons(ids)
                    .map(lessonResponseMapper)
            }
}