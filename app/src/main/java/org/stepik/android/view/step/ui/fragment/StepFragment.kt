package org.stepik.android.view.step.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_step.*
import kotlinx.android.synthetic.main.view_step_quiz_error.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.SolutionStatsSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.StepShareDialogFragment
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.commitNow
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.analytic.reportStepEvent
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Step
import org.stepik.android.presentation.step.StepPresenter
import org.stepik.android.presentation.step.StepView
import org.stepik.android.view.injection.step.StepComponent
import org.stepik.android.view.lesson.ui.interfaces.NextMoveable
import org.stepik.android.view.lesson.ui.interfaces.Playable
import org.stepik.android.view.step.ui.delegate.StepDiscussionsDelegate
import org.stepik.android.view.step.ui.delegate.StepNavigationDelegate
import org.stepik.android.view.step.ui.delegate.StepSolutionStatsDelegate
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFragmentFactory
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class StepFragment : Fragment(), StepView,
    NextMoveable,
    Playable {
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
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var stepContentFragmentFactory: StepContentFragmentFactory

    @Inject
    internal lateinit var stepQuizFragmentFactory: StepQuizFragmentFactory

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var solutionStatsSplitTest: SolutionStatsSplitTest

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    private lateinit var stepComponent: StepComponent
    private lateinit var stepPresenter: StepPresenter

    private lateinit var stepSolutionStatsDelegate: StepSolutionStatsDelegate
    private lateinit var stepNavigationDelegate: StepNavigationDelegate
    private lateinit var stepDiscussionsDelegate: StepDiscussionsDelegate

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()

        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        stepPresenter = ViewModelProviders.of(this, viewModelFactory).get(StepPresenter::class.java)
        stepPresenter.onLessonData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        stepComponent = App
            .componentManager()
            .stepParentComponent(stepWrapper, lessonData)
        stepComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stepSolutionStatsDelegate = StepSolutionStatsDelegate(
            stepSolutionStats,
            stepWrapper.step,
            solutionStatsSplitTest.currentGroup.isStatsVisible && stepQuizFragmentFactory.isStepCanHaveQuiz(stepWrapper)
        )

        stepNavigationDelegate = StepNavigationDelegate(stepNavigation) { stepPresenter.onStepDirectionClicked(it) }

        stepDiscussionsDelegate = StepDiscussionsDelegate(view) { discussionThread ->
            screenManager
                .openComments(
                    activity,
                    discussionThread,
                    stepWrapper.step,
                    null,
                    discussionThread.discussionsCount == 0
                )
        }

        stepStatusTryAgain.setOnClickListener { stepPresenter.fetchStepUpdate(stepWrapper.step.id) }
        initStepContentFragment()
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
                stepContentFragmentFactory.createStepContentFragment(stepWrapper)

            childFragmentManager
                .beginTransaction()
                .add(R.id.stepContentContainer, stepContentFragment, STEP_CONTENT_FRAGMENT_TAG)
                .commitNow()
        }
    }

    private fun setStepQuizFragment(isNeedReload: Boolean) {
        val isStepHasQuiz = stepQuizFragmentFactory.isStepCanHaveQuiz(stepWrapper)
        stepContentSeparator.isVisible = isStepHasQuiz
        stepQuizContainer.isVisible = isStepHasQuiz
        stepQuizError.isVisible = false
        if (isStepHasQuiz) {
            val isQuizFragmentEmpty = childFragmentManager.findFragmentByTag(STEP_QUIZ_FRAGMENT_TAG) == null

            if (isQuizFragmentEmpty || isNeedReload) {
                val quizFragment = stepQuizFragmentFactory.createStepQuizFragment(stepWrapper)

                childFragmentManager.commitNow {
                    if (isQuizFragmentEmpty) {
                        add(R.id.stepQuizContainer, quizFragment, STEP_QUIZ_FRAGMENT_TAG)
                    } else {
                        replace(R.id.stepQuizContainer, quizFragment, STEP_QUIZ_FRAGMENT_TAG)
                    }
                }
            }
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
        inflater.inflate(R.menu.step_menu, menu)
        menu.findItem(R.id.menu_item_submissions)
            ?.isVisible = stepQuizFragmentFactory.isStepCanHaveQuiz(stepWrapper)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_share -> {
                showShareDialog()
                true
            }

            R.id.menu_item_submissions -> {
                showSubmissionsDialog()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }

    private fun showShareDialog() {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?: return

        StepShareDialogFragment
            .newInstance(stepWrapper.step, lessonData.lesson, lessonData.unit)
            .showIfNotExists(supportFragmentManager, StepShareDialogFragment.TAG)
    }

    private fun showSubmissionsDialog() {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?: return

        SubmissionsDialogFragment
            .newInstance(stepWrapper.step)
            .showIfNotExists(supportFragmentManager, SubmissionsDialogFragment.TAG)

        analytic
            .reportStepEvent(AmplitudeAnalytic.Steps.STEP_SOLUTIONS_OPENED, AmplitudeAnalytic.Steps.STEP_SOLUTIONS_OPENED, stepWrapper.step)
    }

    override fun setState(state: StepView.State) {
        if (state is StepView.State.Loaded) {
            val isNeedReloadQuiz = stepWrapper.step.block != state.stepWrapper.step.block

            stepWrapper = state.stepWrapper
            stepDiscussionsDelegate.setDiscussionThreads(state.discussionThreads)
            when (stepWrapper.step.status) {
                Step.Status.READY ->
                    setStepQuizFragment(isNeedReloadQuiz)
                Step.Status.PREPARING,
                Step.Status.ERROR -> {
                    stepContentSeparator.isVisible = true
                    stepQuizContainer.isVisible = false
                    stepQuizError.isVisible = true
                }
            }
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
        stepQuizContainer.layoutParams = (stepQuizContainer.layoutParams as ViewGroup.MarginLayoutParams)
            .apply {
                bottomMargin =
                    if (stepNavigation.visibility == View.VISIBLE) {
                        0
                    } else {
                        resources.getDimensionPixelSize(R.dimen.step_quiz_container_bottom_margin)
                    }
            }
    }

    override fun showLesson(direction: StepNavigationDirection, lessonData: LessonData, isAutoplayEnabled: Boolean) {
        val unit = lessonData.unit ?: return
        val section = lessonData.section ?: return

        screenManager.showSteps(activity, unit, lessonData.lesson, section, direction == StepNavigationDirection.PREV, isAutoplayEnabled)
        activity?.finish()
    }

    override fun moveNext(isAutoplayEnabled: Boolean): Boolean {
        if ((activity as? NextMoveable)?.moveNext(isAutoplayEnabled) != true) {
            stepPresenter.onStepDirectionClicked(StepNavigationDirection.NEXT, isAutoplayEnabled)
        }
        return true
    }

    override fun play(): Boolean =
        (childFragmentManager.findFragmentByTag(STEP_CONTENT_FRAGMENT_TAG) as? Playable)
            ?.play()
            ?: false
}