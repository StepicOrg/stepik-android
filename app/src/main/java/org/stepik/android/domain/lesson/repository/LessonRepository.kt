package org.stepik.android.domain.lesson.repository

import io.reactivex.Maybe
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.Lesson

interface LessonRepository {
    fun getLesson(lessonId: Long, primarySourceType: DataSourceType = DataSourceType.CACHE): Maybe<Lesson>
}