package org.stepic.droid.storage

import org.stepik.android.model.structure.Lesson
import org.stepic.droid.model.Step

interface CleanManager {

    fun removeSection(sectionId: Long)

    fun removeLesson(lesson: Lesson?)

    fun removeStep(step: Step?)
}
