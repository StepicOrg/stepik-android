package org.stepik.android.view.lesson.ui.mapper

import android.content.Context
import org.stepic.droid.R
import org.stepik.android.domain.lesson.model.LessonData
import javax.inject.Inject

class LessonTitleMapper
@Inject
constructor() {
    fun mapToLessonTitle(context: Context, lessonData: LessonData): String =
        if (lessonData.section != null && lessonData.unit != null) {
            context.getString(R.string.lesson_toolbar_title, lessonData.section.position, lessonData.unit.position, lessonData.lesson.title)
        } else {
            lessonData.lesson.title.orEmpty()
        }
}