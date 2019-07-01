package org.stepik.android.view.step_quiz_text.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_step_quiz_text.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz_text.TextStepQuizPresenter
import org.stepik.android.presentation.step_quiz_text.TextStepQuizView
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import javax.inject.Inject

class TextStepQuizFragment : Fragment(), TextStepQuizView {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper): Fragment =
            TextStepQuizFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: TextStepQuizPresenter

    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    private lateinit var stepQuizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate
    private lateinit var textStepQuizFormDelegate: TextStepQuizFormDelegate
    private lateinit var stepQuizDelegate: StepQuizDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()

        presenter = ViewModelProviders.of(this, viewModelFactory).get(TextStepQuizPresenter::class.java)
        presenter.onStepData(stepWrapper.step.id)
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_text, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizFeedbackBlocksDelegate = StepQuizFeedbackBlocksDelegate(stepQuizFeedbackBlocks)
        textStepQuizFormDelegate = TextStepQuizFormDelegate(stringStepQuizField)
        stepQuizDelegate = StepQuizDelegate(textStepQuizFormDelegate, stepQuizFeedbackBlocksDelegate, stepQuizSubmit, presenter::createSubmission)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: TextStepQuizView.State) {
        stepQuizDelegate.setState(state)
    }

    override fun showNetworkError() {
        val view = view ?: return

        Snackbar
            .make(view, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            .show()
    }
}