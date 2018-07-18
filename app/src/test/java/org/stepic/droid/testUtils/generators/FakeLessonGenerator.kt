package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Lesson

object FakeLessonGenerator {

    fun generate(stepIds: LongArray): Lesson {
        val lesson = Lesson()
        lesson.steps = stepIds
        return lesson
    }

}
