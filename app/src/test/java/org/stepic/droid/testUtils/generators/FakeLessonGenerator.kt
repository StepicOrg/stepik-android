package org.stepic.droid.testUtils.generators

import org.stepic.droid.model.Lesson

object FakeLessonGenerator {

    fun generate(stepIds: LongArray): Lesson {
        val lesson = Lesson()
        lesson.steps = stepIds
        return lesson
    }

}
