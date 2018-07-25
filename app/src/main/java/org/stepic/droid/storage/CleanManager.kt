package org.stepic.droid.storage

import org.stepik.android.model.Lesson
import org.stepik.android.model.Step

interface CleanManager {

    fun removeSection(sectionId: Long)

    fun removeLesson(lesson: Lesson?)

    fun removeStep(step: Step?)
}
