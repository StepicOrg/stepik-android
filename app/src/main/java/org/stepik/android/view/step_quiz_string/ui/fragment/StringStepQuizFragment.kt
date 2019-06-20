package org.stepik.android.view.step_quiz_string.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData

class StringStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
            StringStepQuizFragment()
                .apply {
//                    this.lessonData = lessonData
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_string, container, false)
}