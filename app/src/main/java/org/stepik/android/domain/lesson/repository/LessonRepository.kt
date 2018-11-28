package org.stepik.android.domain.lesson.repository

import io.reactivex.Maybe
import org.stepik.android.model.Lesson

interface LessonRepository {
    fun getLesson(lessonId: Long): Maybe<Lesson>
}