package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Lesson

object FakeLessonGenerator {

    @JvmOverloads
    fun generate(id: Long = 0, stepIds: LongArray): Lesson = Lesson(id = id, steps = stepIds)

}
