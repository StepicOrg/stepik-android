package org.stepik.android.view.step_quiz_text.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_text.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate

class TextStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
            TextStepQuizFragment()
                .apply {
//                    this.lessonData = lessonData
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    private lateinit var stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_text, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizSubmit.setOnClickListener { }
        stepQuizSubmit.isEnabled = true

        stepQuizFeedbackBlocksDelegate = StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks)

        stepQuizFeedbackBlocksDelegate.setState(StepQuizFeedbackState.Wrong(hint = "Lorem ipsum dit aleri poel pelmeni"))
    }
}