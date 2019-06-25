package org.stepik.android.view.step_quiz_choice.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_choice.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.attempts.Dataset
import org.stepik.android.model.attempts.DatasetWrapper
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceQuizFormDelegate

class ChoiceStepQuizFragment: Fragment() {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
                ChoiceStepQuizFragment()
                    .apply {
                        this.stepWrapper = stepPersistentWrapper
                    }
    }

    private var stepWrapper: StepPersistentWrapper by argument()
    private lateinit var choiceQuizFormDelegate: ChoiceQuizFormDelegate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_choice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        choiceQuizFormDelegate = ChoiceQuizFormDelegate(choice_quiz_attempt)
        choiceQuizFormDelegate.setAttempt(Attempt(
            _dataset = DatasetWrapper(
                Dataset(
                    options = listOf("Variant 1", "Variant 2", "Variant 3\nExtra line", "Variant 4"),
                    isMultipleChoice = false
                )
            )
        ))
    }
}