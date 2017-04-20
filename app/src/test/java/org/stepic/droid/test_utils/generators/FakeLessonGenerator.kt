package org.stepic.droid.test_utils.generators

import org.stepic.droid.model.Lesson

object FakeLessonGenerator {

    @JvmOverloads
    fun generate(stepIds: LongArray): Lesson {
        val lesson = Lesson()
        lesson.steps = stepIds
        return lesson
    }

}
