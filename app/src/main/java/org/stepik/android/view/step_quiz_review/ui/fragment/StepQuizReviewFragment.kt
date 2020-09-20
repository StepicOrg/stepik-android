package org.stepik.android.view.step_quiz_review.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class StepQuizReviewFragment : Fragment(R.layout.fragment_step_quiz_review), StepQuizReviewView {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            StepQuizReviewFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private var stepId: Long by argument()

    private lateinit var stepQuizReviewPresenter: StepQuizReviewPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizReviewView.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()

        stepQuizReviewPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(StepQuizReviewPresenter::class.java)
    }

    private fun injectComponent() {
        App.componentManager()
            .stepComponent(stepId)
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
    }

    override fun onStart() {
        super.onStart()
        stepQuizReviewPresenter.attachView(this)
    }

    override fun onStop() {
        stepQuizReviewPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: StepQuizReviewView.State) {
        when (state) {
        }
    }

    override fun showNetworkError() {
    }
}