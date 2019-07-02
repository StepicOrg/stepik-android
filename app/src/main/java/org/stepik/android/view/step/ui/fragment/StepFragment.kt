package org.stepik.android.view.step.ui.fragment

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_step.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.StepShareDialogFragment
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.presentation.step.StepPresenter
import org.stepik.android.presentation.step.StepView
import org.stepik.android.view.step.ui.delegate.StepDiscussionsDelegate
import org.stepik.android.view.step.ui.delegate.StepNavigationDelegate
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFragmentFactory
import javax.inject.Inject

class StepFragment : Fragment(), StepView {
    companion object {
        private const val STEP_CONTENT_FRAGMENT_TAG = "step_content"
        private const val STEP_QUIZ_FRAGMENT_TAG = "step_quiz"

        fun newInstance(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            StepFragment()
                .apply {
                    this.stepWrapper = stepWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepContentFragmentFactory: StepContentFragmentFactory

    @Inject
    internal lateinit var stepQuizFragmentFactory: StepQuizFragmentFactory

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var stepPresenter: StepPresenter

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    private lateinit var stepNavigationDelegate: StepNavigationDelegate
    private lateinit var stepDiscussionsDelegate: StepDiscussionsDelegate

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        injectComponent()

        stepPresenter = ViewModelProviders.of(this, viewModelFactory).get(StepPresenter::class.java)
        stepPresenter.onLessonData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stepNavigationDelegate = StepNavigationDelegate(stepNavigation, stepPresenter::onStepDirectionClicked)

        stepDiscussionsDelegate = StepDiscussionsDelegate(stepDiscussions) {
            screenManager
                .openComments(activity, stepWrapper.step.discussionProxy, stepWrapper.step.id, stepWrapper.step.discussionsCount == 0)
        }
        stepDiscussionsDelegate.setDiscussionsCount(stepWrapper.step.discussionsCount)

        initStepContentFragment()
        initStepQuizFragment()
    }

    private fun initStepContentFragment() {
        stepContentContainer.layoutParams = (stepContentContainer.layoutParams as LinearLayoutCompat.LayoutParams)
            .apply {
                if (stepQuizFragmentFactory.isStepCanHaveQuiz(stepWrapper)) {
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    weight = 0f
                } else {
                    height = 0
                    weight = 1f
                }
            }

        if (childFragmentManager.findFragmentByTag(STEP_CONTENT_FRAGMENT_TAG) == null) {
            val stepContentFragment =
                stepContentFragmentFactory.createStepContentFragment(stepWrapper, lessonData)

            childFragmentManager
                .beginTransaction()
                .add(R.id.stepContentContainer, stepContentFragment, STEP_CONTENT_FRAGMENT_TAG)
                .commitNow()
        }
    }

    private fun initStepQuizFragment() {
        val isStepHasQuiz = stepQuizFragmentFactory.isStepCanHaveQuiz(stepWrapper)
        stepQuizContainer.changeVisibility(isStepHasQuiz)
        if (isStepHasQuiz && childFragmentManager.findFragmentByTag(STEP_QUIZ_FRAGMENT_TAG) == null) {
            childFragmentManager
                .beginTransaction()
                .add(R.id.stepQuizContainer, stepQuizFragmentFactory.createStepQuizFragment(stepWrapper), STEP_QUIZ_FRAGMENT_TAG)
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        stepPresenter.attachView(this)
    }

    override fun onStop() {
        stepPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.share_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == R.id.menu_item_share) {
            showShareDialog()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    private fun showShareDialog() {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?.takeIf { it.findFragmentByTag(StepShareDialogFragment.TAG) == null }
            ?: return

        StepShareDialogFragment
            .newInstance(stepWrapper.step, lessonData.lesson, lessonData.unit)
            .show(supportFragmentManager, StepShareDialogFragment.TAG)
    }

    override fun setState(state: StepView.State) {
        if (state is StepView.State.Loaded) {
            stepWrapper = state.stepWrapper
            stepDiscussionsDelegate.setDiscussionsCount(state.stepWrapper.step.discussionsCount)
        }
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun setNavigation(directions: Set<StepNavigationDirection>) {
        stepNavigationDelegate.setState(directions)
    }

    override fun showLesson(direction: StepNavigationDirection, lessonData: LessonData) {
        val unit = lessonData.unit ?: return
        val section = lessonData.section ?: return

        screenManager.showSteps(activity, unit, lessonData.lesson, direction == StepNavigationDirection.PREV, section)
        activity?.finish()
    }
}