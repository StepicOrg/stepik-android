package org.stepic.droid.store

interface LessonDownloader {

    fun downloadLesson(lessonId: Long)

    fun cancelLessonLoading(lessonId: Long)

    fun deleteWholeLesson(lessonId: Long)
}

