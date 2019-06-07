package org.stepik.android.view.step.ui.fragment

import android.support.v4.app.Fragment
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData

class StepFragment : Fragment() {
    companion object {
        fun newInstance(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            StepFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                    this.lessonData = lessonData
                }
    }

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()


}