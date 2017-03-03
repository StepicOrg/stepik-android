package org.stepic.droid.store

import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepic.droid.model.Step

interface CleanManager {

    fun removeSection(section: Section?)

    fun removeLesson(lesson: Lesson?)

    fun removeStep(step: Step?)
}
