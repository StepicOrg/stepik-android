package org.stepik.android.view.step.ui.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.fragment_step.*
import kotlinx.android.synthetic.main.view_step_disabled.view.*
import kotlinx.android.synthetic.main.view_step_quiz_error.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.StepShareDialogFragment
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.StringUtil
import org.stepic.droid.util.commitNow
import org.stepic.droid.util.copyTextToClipboard
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData
import org.stepik.android.domain.step.analytic.reportStepEvent
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Step
import org.stepik.android.presentation.step.StepPresenter
import org.stepik.android.presentation.step.StepView
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course_complete.ui.dialog.CourseCompleteBottomSheetDialogFragment
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.injection.step.StepComponent
import org.stepik.android.view.lesson.ui.dialog.LessonDemoCompleteBottomSheetDialogFragment
import org.stepik.android.view.lesson.ui.dialog.SectionUnavailableDialogFragment
import org.stepik.android.view.lesson.ui.interfaces.Moveable
import org.stepik.android.view.lesson.ui.interfaces.Playable
import org.stepik.android.view.lesson.ui.mapper.LessonTitleMapper
import org.stepik.android.view.step.model.StepNavigationAction
import org.stepik.android.view.step.ui.delegate.StepDiscussionsDelegate
import org.stepik.android.view.step.ui.delegate.StepNavigationDelegate
import org.stepik.android.view.step.ui.delegate.StepSolutionStatsDelegate
import org.stepik.android.view.step.ui.interfaces.StepMenuNavigator
import org.stepik.android.view.step_content.ui.factory.StepContentFragmentFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFragmentFactory
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.base.ui.extension.snackbar
import javax.inject.Inject

class StepFragment : Fragment(R.layout.fragment_step), StepView,
    Moveable,
    Playable,
    StepMenuNavigator,
    SectionUnavailableDialogFragment.Callback,
    CourseCompleteBottomSheetDialogFragment.Callback {
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
    internal lateinit var remoteConfig: FirebaseRemoteConfig

    @Inject
    internal lateinit var lessonTitleMapper: LessonTitleMapper

    @Inject
    internal lateinit var config: Config

    private var stepWrapper: StepPersistentWrapper by argument()
    private var lessonData: LessonData by argument()

    private lateinit var stepComponent: StepComponent
    private val stepPresenter: StepPresenter by viewModels { viewModelFactory }

    private lateinit var stepSolutionStatsDelegate: StepSolutionStatsDelegate
    private lateinit var stepNavigationDelegate: StepNavigationDelegate
    private lateinit var stepDiscussionsDelegate: StepDiscussionsDelegate

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        injectComponent()

        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        stepPresenter.onLessonData(stepWrapper, lessonData)
    }

    private fun injectComponent() {
        stepComponent = App
            .componentManager()
            .stepParentComponent(stepWrapper, lessonData)
        stepComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stepSolutionStatsDelegate = StepSolutionStatsDelegate(
            stepSolutionStats,
            stepWrapper.step,
            stepWrapper.isStepCanHaveQuiz
        )

        stepNavigationDelegate = StepNavigationDelegate(stepNavigation) { stepPresenter.onStepDirectionClicked(it) }

        stepDiscussionsDelegate = StepDiscussionsDelegate(view) { discussionThread ->
            analytic.reportAmplitudeEvent(
                AmplitudeAnalytic.Discussions.SCREEN_OPENED,
                mapOf(AmplitudeAnalytic.Discussions.Params.SOURCE to AmplitudeAnalytic.Discussions.Values.DEFAULT)
            )
            screenManager
                .openComments(
                    activity,
                    discussionThread,
                    stepWrapper.step,
                    null,
                    discussionThread.discussionsCount == 0,
                    lessonData.lesson.isTeacher
                )
        }

        stepContentNext.isVisible = isStepContentNextVisible(stepWrapper, lessonData)
        stepContentNext.setOnClickListener { move() }
        stepStatusTryAgain.setOnClickListener { stepPresenter.fetchStepUpdate(stepWrapper.step.id) }

        initDisabledStep()
        initDisabledStepTeacher()
        initStepContentFragment()
    }

    private fun initDisabledStep() {
        val lessonTitle =
            lessonTitleMapper.mapToLessonTitle(requireContext(), lessonData)
        val stepTitle =
            getString(R.string.step_disabled_student_pattern, lessonTitle, stepWrapper.step.position)

        val stepLinkSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val stepUri = StringUtil
                    .getUriForStep(config.baseUrl, lessonData.lesson, lessonData.unit, stepWrapper.step)

                requireContext()
                    .copyTextToClipboard(textToCopy = stepUri, toastMessage = getString(R.string.link_copied_title))
            }
        }
        val placeholderMessage = stepDisabled.placeholderMessage
        placeholderMessage.text =
            buildSpannedString {
                append(getString(R.string.step_disabled_student_description_part_1))
                inSpans(stepLinkSpan) {
                    append(stepTitle)
                }
                append(getString(R.string.step_disabled_student_description_part_2))
            }
        placeholderMessage.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initDisabledStepTeacher() {
        val tariffTitle = getString(R.string.step_disabled_teacher_tariff_title)
        val tariffLinkSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val tariffInfoUrl = getString(R.string.step_disabled_teacher_tariff_url)

                InAppWebViewDialogFragment
                    .newInstance(tariffTitle, tariffInfoUrl, isProvideAuth = false)
                    .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
            }
        }

        val isInCourse = lessonData.section != null && lessonData.unit != null

        val planDescription1 = if (stepWrapper.step.needsPlan != null) {
            if (isInCourse) {
                getString(R.string.step_disabled_teacher_plan_description_with_course)
            } else {
                getString(R.string.step_disabled_teacher_plan_description_without_course)
            }
        } else {
            if (isInCourse) {
                getString(R.string.step_disabled_teacher_plan_none_description_with_course)
            } else {
                getString(R.string.step_disabled_teacher_plan_none_description_without_course)
            }
        }

        val planDescription2 = when (stepWrapper.step.needsPlan) {
            Step.PLAN_PRO -> {
                if (isInCourse) {
                    getText(R.string.step_disabled_teacher_plan_pro_with_course)
                } else {
                    getText(R.string.step_disabled_teacher_plan_pro_without_course)
                }
            }
            Step.PLAN_ENTERPRISE -> {
                if (isInCourse) {
                    getText(R.string.step_disabled_teacher_enterprise_with_course)
                } else {
                    getText(R.string.step_disabled_teacher_plan_enterprise_without_course)
                }
            }
            else ->
                ""
        }

        val placeholderMessage = stepDisabledTeacher.placeholderMessage
        placeholderMessage.text =
            buildSpannedString {
                append(planDescription1)
                append(planDescription2)
                append(getString(R.string.step_disabled_teacher_tariff_description))
                inSpans(tariffLinkSpan) {
                    append(tariffTitle)
                }
                append(getString(R.string.full_stop))
            }
        placeholderMessage.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun initStepContentFragment() {
        stepContentContainer.layoutParams = (stepContentContainer.layoutParams as LinearLayoutCompat.LayoutParams)
            .apply {
                if (stepWrapper.isStepCanHaveQuiz) {
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
        val isStepHasQuiz = stepWrapper.isStepCanHaveQuiz
        stepContentSeparator.isVisible = isStepHasQuiz
        stepQuizContainer.isVisible = isStepHasQuiz
        stepQuizError.isVisible = false
        if (isStepHasQuiz) {
            val isQuizFragmentEmpty = childFragmentManager.findFragmentByTag(STEP_QUIZ_FRAGMENT_TAG) == null

            if (isQuizFragmentEmpty || isNeedReload) {
                val quizFragment = stepQuizFragmentFactory.createStepQuizFragment(stepWrapper, lessonData)

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
            ?.isVisible = stepWrapper.isStepCanHaveQuiz
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_item_share -> {
                showShareDialog()
                true
            }

            R.id.menu_item_submissions -> {
                showSubmissions()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }

    override fun showSubmissions() {
        val instructionId = stepWrapper.step.instruction
        if (instructionId != null) {
            stepPresenter.onFetchReviewInstruction(instructionId)
        } else {
            showSubmissionsDialog(reviewInstructionData = null)
        }
    }

    override fun showShareDialog() {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?: return

        StepShareDialogFragment
            .newInstance(stepWrapper.step, lessonData.lesson, lessonData.unit)
            .showIfNotExists(supportFragmentManager, StepShareDialogFragment.TAG)
    }

    private fun showSubmissionsDialog(reviewInstructionData: ReviewInstructionData?) {
        val supportFragmentManager = activity
            ?.supportFragmentManager
            ?: return

        SubmissionsDialogFragment
            .newInstance(stepWrapper.step, isTeacher = lessonData.lesson.isTeacher, reviewInstructionData = reviewInstructionData)
            .showIfNotExists(supportFragmentManager, SubmissionsDialogFragment.TAG)

        analytic
            .reportStepEvent(AmplitudeAnalytic.Steps.STEP_SOLUTIONS_OPENED, stepWrapper.step)
    }

    override fun setState(state: StepView.State) {
        if (state is StepView.State.Loaded) {
            val isNeedReloadQuiz = stepWrapper.step.block != state.stepWrapper.step.block ||
                    stepWrapper.step.isEnabled != state.stepWrapper.step.isEnabled

            val isStepDisabled = remoteConfig.getBoolean(RemoteConfig.IS_DISABLED_STEPS_SUPPORTED) &&
                    state.stepWrapper.step.isEnabled == false

            val isStepUnavailable = isStepDisabled && !lessonData.lesson.isTeacher

            stepContentContainer.isGone = isStepUnavailable
            stepContentSeparator.isGone = isStepUnavailable
            stepQuizError.isGone = isStepUnavailable
            stepQuizContainer.isGone = isStepUnavailable
            stepFooter.isGone = isStepUnavailable

            stepDisabled.isVisible = isStepUnavailable
            stepDisabledTeacher.isVisible = isStepDisabled && lessonData.lesson.isTeacher
            stepContentNext.isVisible = isStepContentNextVisible(state.stepWrapper, lessonData)

            stepWrapper = state.stepWrapper

            if (!isStepDisabled || lessonData.lesson.isTeacher) {
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
    }

    private fun isStepContentNextVisible(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Boolean {
        val isStepDisabled = remoteConfig.getBoolean(RemoteConfig.IS_DISABLED_STEPS_SUPPORTED) &&
                stepWrapper.step.isEnabled == false

        val isStepNotLast = stepWrapper.step.position < lessonData.lesson.steps.size

        return ((isStepDisabled && !lessonData.lesson.isTeacher) || !stepWrapper.isStepCanHaveQuiz) && isStepNotLast
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
        val actionBottomMargin =  if (stepNavigation.visibility == View.VISIBLE) {
            0
        } else {
            resources.getDimensionPixelSize(R.dimen.step_quiz_container_bottom_margin)
        }
        stepQuizContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = actionBottomMargin
        }
        stepContentNext.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = actionBottomMargin
        }
    }

    override fun handleNavigationAction(stepNavigationAction: StepNavigationAction) {
        when (stepNavigationAction) {
            is StepNavigationAction.ShowLesson -> {
                val unit = stepNavigationAction.lessonData.unit ?: return
                val section = stepNavigationAction.lessonData.section ?: return

                activity?.finish()
                screenManager.showSteps(
                    activity,
                    unit,
                    stepNavigationAction.lessonData.lesson,
                    section,
                    stepNavigationAction.direction == StepNavigationDirection.PREV,
                    stepNavigationAction.isAutoplayEnabled
                )
            }

            is StepNavigationAction.ShowLessonDemoComplete ->
                LessonDemoCompleteBottomSheetDialogFragment
                    .newInstance(stepNavigationAction.course)
                    .showIfNotExists(childFragmentManager, LessonDemoCompleteBottomSheetDialogFragment.TAG)

            is StepNavigationAction.ShowSectionUnavailable ->
                SectionUnavailableDialogFragment
                    .newInstance(stepNavigationAction.sectionUnavailableAction)
                    .showIfNotExists(childFragmentManager, SectionUnavailableDialogFragment.TAG)

            is StepNavigationAction.ShowCourseComplete ->
                CourseCompleteBottomSheetDialogFragment
                    .newInstance(stepNavigationAction.course)
                    .showIfNotExists(childFragmentManager, CourseCompleteBottomSheetDialogFragment.TAG)

            is StepNavigationAction.Unknown ->
                view?.snackbar(messageRes = R.string.step_navigation_action_unknown, length = Snackbar.LENGTH_LONG)
        }
    }

    override fun showQuizReloadMessage() {
        view?.snackbar(messageRes = R.string.step_quiz_reload_message, length = Snackbar.LENGTH_LONG)
    }

    override fun openShowSubmissionsWithReview(reviewInstructionData: ReviewInstructionData) {
        showSubmissionsDialog(reviewInstructionData = reviewInstructionData)
    }

    override fun move(isAutoplayEnabled: Boolean, stepNavigationDirection: StepNavigationDirection): Boolean {
        if ((activity as? Moveable)?.move(isAutoplayEnabled, stepNavigationDirection) != true) {
            stepPresenter.onStepDirectionClicked(stepNavigationDirection, isAutoplayEnabled)
        }
        return true
    }

    override fun play(): Boolean =
        (childFragmentManager.findFragmentByTag(STEP_CONTENT_FRAGMENT_TAG) as? Playable)
            ?.play()
            ?: false

    override fun onSyllabusAction(courseViewSource: CourseViewSource) {
        val course = lessonData.course ?: return
        screenManager.showCourseFromNavigationDialog(requireContext(), course.id, courseViewSource, CourseScreenTab.SYLLABUS, false)
    }

    override fun showErrorMessage() {
        view?.snackbar(messageRes = R.string.step_navigation_action_unknown, length = Snackbar.LENGTH_LONG)
    }
}