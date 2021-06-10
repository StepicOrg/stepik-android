package org.stepik.android.view.course_complete.ui.dialog

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.SpannedString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.bottom_sheet_dialog_course_complete.*
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ShareHelper
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_complete.analytic.FinishedStepsBackToAssignmentsPressedAnalyticEvent
import org.stepik.android.domain.course_complete.analytic.FinishedStepsFindNewCoursePressedAnalyticEvent
import org.stepik.android.domain.course_complete.analytic.FinishedStepsLeaveReviewPressedAnalyticEvent
import org.stepik.android.domain.course_complete.analytic.FinishedStepsScreenOpenedAnalyticEvent
import org.stepik.android.domain.course_complete.analytic.FinishedStepsSharePressedAnalyticEvent
import org.stepik.android.domain.course_complete.analytic.FinishedStepsViewCertificatePressedAnalyticEvent
import org.stepik.android.domain.course_complete.model.CourseCompleteInfo
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_complete.CourseCompleteFeature
import org.stepik.android.presentation.course_complete.CourseCompleteViewModel
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course_complete.model.CourseCompleteDialogViewInfo
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class CourseCompleteBottomSheetDialogFragment : BottomSheetDialogFragment(),
    ReduxView<CourseCompleteFeature.State, CourseCompleteFeature.Action.ViewAction> {
    companion object {
        const val TAG = "CourseCompleteBottomSheetDialogFragment"

        const val ARG_COURSE = "course"

        fun newInstance(course: Course): DialogFragment =
            CourseCompleteBottomSheetDialogFragment().apply {
                this.course = course
            }
    }

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var shareHelper: ShareHelper

    private var course: Course by argument()
    private val viewModel: CourseCompleteViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var viewStateDelegate: ViewStateDelegate<CourseCompleteFeature.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_course_complete, container, false)

    private fun injectComponent() {
        App.component()
            .courseCompleteComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytic.report(FinishedStepsScreenOpenedAnalyticEvent(course))
        courseCompleteFeedback.background = AppCompatResources
            .getDrawable(requireContext(), R.drawable.bg_shape_rounded)
            ?.mutate()
            ?.let { DrawableCompat.wrap(it) }
            ?.also {
                DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.color_overlay_violet_alpha_12))
                DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
            }
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CourseCompleteFeature.State.Idle>()
        viewStateDelegate.addState<CourseCompleteFeature.State.Loading>(courseCompleteProgressbar)
        viewStateDelegate.addState<CourseCompleteFeature.State.Content>(
            courseCompleteHeader,
            courseCompleteTitle,
            courseCompleteFeedback,
            courseCompleteSubtitle,
            viewCertificateAction,
            shareResultAction,
            courseCompleteDivider,
            primaryAction,
            secondaryAction
        )
        viewStateDelegate.addState<CourseCompleteFeature.State.NetworkError>(courseCompleteNetworkError)
        viewModel.onNewMessage(CourseCompleteFeature.Message.Init(course))

        tryAgain.setOnClickListener { viewModel.onNewMessage(CourseCompleteFeature.Message.Init(course, forceUpdate = true)) }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun render(state: CourseCompleteFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is CourseCompleteFeature.State.Content) {
            val dialogViewInfo = mapToDialogViewInfo(state.courseCompleteInfo)
            if (dialogViewInfo == CourseCompleteDialogViewInfo.EMPTY) {
                (parentFragment as? Callback)?.showErrorMessage()
                dismiss()
            } else {
                setupDialogView(state.courseCompleteInfo, dialogViewInfo)
            }
        }
    }

    override fun onAction(action: CourseCompleteFeature.Action.ViewAction) {
        // no op
    }

    private fun mapToDialogViewInfo(courseCompleteInfo: CourseCompleteInfo): CourseCompleteDialogViewInfo {
        val score = courseCompleteInfo
            .courseProgress
            .score
            ?.toFloatOrNull()
            ?: 0f

        val cost = courseCompleteInfo.courseProgress.cost
        val progress = score * 100 / cost

        return when {
            progress < 20f && !courseCompleteInfo.course.hasCertificate -> {
                setupCertificateNotIssued(
                    courseCompleteInfo = courseCompleteInfo,
                    headerImage = R.drawable.ic_tak_demo_lesson,
                    gradientRes = R.drawable.course_complete_blue_violet_gradient,
                    isSuccess = false,
                    primaryActionStringRes = R.string.course_complete_action_find_new_course,
                    secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                )
            }
            progress < 20f && courseCompleteInfo.course.hasCertificate -> {
                val courseScore = score.toLong()
                when {
                    courseScore < courseCompleteInfo.course.certificateRegularThreshold -> {
                        setupNotReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_demo_lesson,
                            gradientRes = R.drawable.course_complete_blue_violet_gradient,
                            isSuccess = false,
                            primaryActionStringRes = R.string.course_complete_action_find_new_course,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateRegularThreshold && (courseScore < courseCompleteInfo.course.certificateDistinctionThreshold || courseCompleteInfo.course.certificateDistinctionThreshold == 0L) -> {
                        val distinctionSubtitle = getCertificateDistinction(score.toLong(), courseCompleteInfo.course.certificateDistinctionThreshold)
                        val secondaryActionStringRes = if (courseCompleteInfo.course.certificateDistinctionThreshold == 0L) {
                            R.string.course_complete_action_find_new_course
                        } else {
                            R.string.course_complete_action_back_to_assignments
                        }
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_regular_certificate,
                            gradientRes = R.drawable.course_complete_blue_violet_gradient,
                            distinctionSubtitle = distinctionSubtitle,
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = secondaryActionStringRes
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateDistinctionThreshold -> {
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_distinction_certificate,
                            gradientRes = R.drawable.course_complete_yellow_red_gradient,
                            distinctionSubtitle = SpannedString(""),
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                        )
                    }
                    else ->
                        CourseCompleteDialogViewInfo.EMPTY
                }
            }
            progress >= 20f && progress < 80f && !courseCompleteInfo.course.hasCertificate -> {
                setupCertificateNotIssued(
                    courseCompleteInfo = courseCompleteInfo,
                    headerImage = R.drawable.ic_tak_neutral,
                    gradientRes = R.drawable.course_complete_yellow_green_gradient,
                    isSuccess = true,
                    primaryActionStringRes = R.string.course_complete_action_find_new_course,
                    secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                )
            }
            progress >= 20f && progress < 80f && courseCompleteInfo.course.hasCertificate -> {
                val courseScore = score.toLong()
                when {
                    courseScore < courseCompleteInfo.course.certificateRegularThreshold -> {
                        setupNotReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_neutral,
                            gradientRes = R.drawable.course_complete_yellow_green_gradient,
                            isSuccess = true,
                            primaryActionStringRes = R.string.course_complete_action_find_new_course,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateRegularThreshold && (courseScore < courseCompleteInfo.course.certificateDistinctionThreshold || courseCompleteInfo.course.certificateDistinctionThreshold == 0L) -> {
                        val distinctionSubtitle = getCertificateDistinction(score.toLong(), courseCompleteInfo.course.certificateDistinctionThreshold)
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_regular_certificate,
                            gradientRes = R.drawable.course_complete_blue_violet_gradient,
                            distinctionSubtitle = distinctionSubtitle,
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assignments
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateDistinctionThreshold -> {
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_distinction_certificate,
                            gradientRes = R.drawable.course_complete_yellow_red_gradient,
                            distinctionSubtitle = SpannedString(""),
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = R.string.course_complete_action_find_new_course
                        )
                    }
                    else ->
                        CourseCompleteDialogViewInfo.EMPTY
                }
            }
            progress >= 80f && !courseCompleteInfo.course.hasCertificate -> {
                val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                    -1 to R.string.course_complete_action_find_new_course
                } else {
                    R.string.course_complete_action_find_new_course to R.string.course_complete_action_leave_review
                }

                setupCertificateNotIssued(
                    courseCompleteInfo = courseCompleteInfo,
                    headerImage = R.drawable.ic_tak_success,
                    gradientRes = R.drawable.course_complete_yellow_red_gradient,
                    isSuccess = true,
                    primaryActionStringRes = primaryAction,
                    secondaryActionStringRes = secondaryAction
                )
            }

            progress >= 80f && courseCompleteInfo.course.hasCertificate -> {
                val courseScore = score.toLong()
                when {
                    courseScore < courseCompleteInfo.course.certificateRegularThreshold -> {
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_back_to_assignments
                        } else {
                            R.string.course_complete_action_leave_review to R.string.course_complete_action_back_to_assignments
                        }
                        setupNotReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_neutral,
                            gradientRes = R.drawable.course_complete_yellow_green_gradient,
                            isSuccess = true,
                            primaryActionStringRes = primaryAction,
                            secondaryActionStringRes = secondaryAction
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateRegularThreshold && (courseScore < courseCompleteInfo.course.certificateDistinctionThreshold || courseCompleteInfo.course.certificateDistinctionThreshold == 0L) -> {
                        val distinctionSubtitle = getCertificateDistinction(score.toLong(), courseCompleteInfo.course.certificateDistinctionThreshold)
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_back_to_assignments
                        } else {
                            R.string.course_complete_action_leave_review to R.string.course_complete_action_back_to_assignments
                        }
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_regular_certificate,
                            gradientRes = R.drawable.course_complete_blue_violet_gradient,
                            distinctionSubtitle = distinctionSubtitle,
                            primaryActionStringRes = primaryAction,
                            secondaryActionStringRes = secondaryAction
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateDistinctionThreshold -> {
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            -1 to R.string.course_complete_action_find_new_course
                        } else {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_leave_review
                        }
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_distinction_certificate,
                            gradientRes = R.drawable.course_complete_yellow_red_gradient,
                            distinctionSubtitle = SpannedString(""),
                            primaryActionStringRes = primaryAction,
                            secondaryActionStringRes = secondaryAction
                        )
                    }
                    else ->
                        CourseCompleteDialogViewInfo.EMPTY
                }
            }
            else ->
                CourseCompleteDialogViewInfo.EMPTY
        }
    }

    /**
     * Setup function for cases when no certificate is issued
     */
    private fun setupCertificateNotIssued(
        courseCompleteInfo: CourseCompleteInfo,
        @DrawableRes
        headerImage: Int,
        @DrawableRes
        gradientRes: Int,
        isSuccess: Boolean,
        primaryActionStringRes: Int,
        secondaryActionStringRes: Int
    ): CourseCompleteDialogViewInfo {
        val score = courseCompleteInfo
            .courseProgress
            .score
            ?.toFloatOrNull()
            ?: 0f

        val cost = courseCompleteInfo.courseProgress.cost
        val progress = score * 100 / cost

        val title = if (isSuccess) {
            getString(
                R.string.course_complete_title_with_success,
                courseCompleteInfo.course.title.toString()
            )
        } else {
            getString(
                R.string.course_complete_title_finished_course,
                courseCompleteInfo.course.title.toString()
            )
        }

        val subtitleText = buildSpannedString {
            append(getSubtitleTextBeginning(score.toInt(), progress.toInt(), cost))
            append(" ")
            if (isSuccess) {
                append(getString(R.string.course_complete_subtitle_not_issued_success))
            } else {
                append(getString(R.string.course_complete_subtitle_not_issued_neutral))
                append("\n\n")
                append(getString(R.string.course_complete_subtitle_continue_message))
            }
        }

        return CourseCompleteDialogViewInfo(
            headerImage,
            gradientRes,
            title,
            SpannedString(""),
            subtitleText,
            isShareVisible = isSuccess,
            isViewCertificateVisible = false,
            primaryActionStringRes = primaryActionStringRes,
            secondaryActionStringRes = secondaryActionStringRes
        )
    }

    /**
     * Setup function for cases when a certificate can be issued but has not been received
     */
    private fun setupNotReceivedCertificate(
        courseCompleteInfo: CourseCompleteInfo,
        @DrawableRes
        headerImage: Int,
        @DrawableRes
        gradientRes: Int,
        isSuccess: Boolean,
        primaryActionStringRes: Int,
        secondaryActionStringRes: Int
    ): CourseCompleteDialogViewInfo {
        val score = courseCompleteInfo
            .courseProgress
            .score
            ?.toFloatOrNull()
            ?: 0f

        val cost = courseCompleteInfo.courseProgress.cost
        val progress = score * 100 / cost

        val title = if (isSuccess) {
            getString(
                R.string.course_complete_title_with_success,
                courseCompleteInfo.course.title.toString()
            )
        } else {
            getString(
                R.string.course_complete_title_finished_course,
                courseCompleteInfo.course.title.toString()
            )
        }

        val subtitleText = buildSpannedString {
            append(getSubtitleTextBeginning(score.toInt(), progress.toInt(), cost))
            append(" ")
            append(getCertificateIssuedText(courseCompleteInfo.course))
            append("\n\n")
            append(getString(R.string.course_complete_subtitle_certificate_hint))
        }

        val neededPoints = courseCompleteInfo.course.certificateRegularThreshold - score.toLong()
        val feedbackText = getFeedbackText(neededPoints)

        return CourseCompleteDialogViewInfo(
            headerImage,
            gradientRes,
            title,
            feedbackText,
            subtitleText,
            isShareVisible = isSuccess,
            isViewCertificateVisible = false,
            primaryActionStringRes = primaryActionStringRes,
            secondaryActionStringRes = secondaryActionStringRes
        )
    }

    /**
     * Setup function for cases when the user has received a certificate
     */

    private fun setupReceivedCertificate(
        courseCompleteInfo: CourseCompleteInfo,
        @DrawableRes
        headerImage: Int,
        @DrawableRes
        gradientRes: Int,
        distinctionSubtitle: SpannedString,
        primaryActionStringRes: Int,
        secondaryActionStringRes: Int
    ): CourseCompleteDialogViewInfo {
        val score = courseCompleteInfo
            .courseProgress
            .score
            ?.toFloatOrNull()
            ?: 0f

        val cost = courseCompleteInfo.courseProgress.cost
        val progress = score * 100 / cost

        val subtitleText = buildSpannedString {
            append(getSubtitleTextBeginning(score.toInt(), progress.toInt(), cost))
            if (distinctionSubtitle.isNotEmpty()) {
                append(" ")
                append(distinctionSubtitle)
            }
            append("\n\n")
            if (courseCompleteInfo.certificate != null) {
                append(getString(R.string.course_complete_subtitle_certificate_ready))
            } else {
                append(getString(R.string.course_complete_subtitle_certificate_not_ready_notify))
                append(" ")
                append(getString(R.string.course_complete_subtitle_certificate_hint))
            }
        }
        return CourseCompleteDialogViewInfo(
            headerImage,
            gradientRes,
            getString(
                R.string.course_complete_title_finished_with_success_and_certificate,
                courseCompleteInfo.course.title.toString()
            ),
            SpannedString(""),
            subtitleText,
            isShareVisible = true,
            isViewCertificateVisible = courseCompleteInfo.certificate != null,
            primaryActionStringRes = primaryActionStringRes,
            secondaryActionStringRes = secondaryActionStringRes
        )
    }

    /**
     * Setup functions for dialog view
     */
    private fun setupDialogView(courseCompleteInfo: CourseCompleteInfo, courseCompleteDialogViewInfo: CourseCompleteDialogViewInfo) {
        courseCompleteLogo.setImageResource(courseCompleteDialogViewInfo.headerImage)
        courseCompleteHeader.setBackgroundResource(courseCompleteDialogViewInfo.gradientRes)
        courseCompleteTitle.text = courseCompleteDialogViewInfo.title
        courseCompleteFeedback.text = courseCompleteDialogViewInfo.feedbackText
        courseCompleteFeedback.isVisible = courseCompleteDialogViewInfo.feedbackText.isNotEmpty()
        courseCompleteSubtitle.text = courseCompleteDialogViewInfo.subtitle

        viewCertificateAction.isVisible = courseCompleteDialogViewInfo.isViewCertificateVisible
        shareResultAction.isVisible = courseCompleteDialogViewInfo.isShareVisible

        val score = courseCompleteInfo
            .courseProgress
            .score
            ?.toFloatOrNull()
            ?: 0f

        val cost = courseCompleteInfo.courseProgress.cost
        val completeRate = (score * 100 / cost) / 100f

        viewCertificateAction.setOnClickListener {
            if (courseCompleteInfo.certificate == null) return@setOnClickListener
            analytic.report(FinishedStepsViewCertificatePressedAnalyticEvent(courseCompleteInfo.course, completeRate))
            screenManager.showPdfInBrowserByGoogleDocs(requireActivity(), courseCompleteInfo.certificate.url)
        }

        shareResultAction.setOnClickListener {
            analytic.report(FinishedStepsSharePressedAnalyticEvent(courseCompleteInfo.course, completeRate))
            if (courseCompleteInfo.certificate != null) {
                val message = getString(R.string.course_complete_share_result_with_certificate, courseCompleteInfo.course.title.toString())
                startActivity(shareHelper.getIntentForCourseResultCertificateSharing(courseCompleteInfo.certificate, message))
            } else {
                val message = getString(R.string.course_complete_share_result, score.toLong(), cost, courseCompleteInfo.course.title.toString())
                startActivity(shareHelper.getIntentForCourseResultSharing(courseCompleteInfo.course, message))
            }
        }

        if (courseCompleteDialogViewInfo.primaryActionStringRes != -1) {
            primaryAction.isVisible = true
            primaryAction.setText(courseCompleteDialogViewInfo.primaryActionStringRes)
            setupOnActionClickListener(
                courseCompleteDialogViewInfo.primaryActionStringRes,
                courseCompleteInfo.course,
                completeRate,
                primaryAction
            )
        } else {
            primaryAction.isVisible = false
        }

        if (courseCompleteDialogViewInfo.secondaryActionStringRes != -1) {
            secondaryAction.isVisible = true
            secondaryAction.setText(courseCompleteDialogViewInfo.secondaryActionStringRes)
            setupOnActionClickListener(
                courseCompleteDialogViewInfo.secondaryActionStringRes,
                courseCompleteInfo.course,
                completeRate,
                secondaryAction
            )
        } else {
            secondaryAction.isVisible = false
        }

        courseCompleteDivider.isVisible = primaryAction.isVisible || secondaryAction.isVisible
    }

    private fun setupOnActionClickListener(actionStringRes: Int, course: Course, completeRate: Float, actionButton: MaterialButton) {
        actionButton.setOnClickListener {
            when (actionStringRes) {
                R.string.course_complete_action_back_to_assignments -> {
                    analytic.report(FinishedStepsBackToAssignmentsPressedAnalyticEvent(course, completeRate))
                    screenManager.showCourseFromNavigationDialog(requireContext(), course.id, CourseViewSource.CourseCompleteDialog, CourseScreenTab.SYLLABUS, false)
                }
                R.string.course_complete_action_find_new_course -> {
                    analytic.report(FinishedStepsFindNewCoursePressedAnalyticEvent(course, completeRate))
                    screenManager.showMainFeed(requireActivity(), MainFeedActivity.CATALOG_INDEX)
                }
                R.string.course_complete_action_leave_review -> {
                    analytic.report(FinishedStepsLeaveReviewPressedAnalyticEvent(course, completeRate))
                    screenManager.showCourseFromNavigationDialog(requireContext(), course.id, CourseViewSource.CourseCompleteDialog, CourseScreenTab.REVIEWS, false)
                }
            }
        }
    }

    /**
     * Setup functions for ui elements
     */
    private fun getSubtitleTextBeginning(score: Int, progress: Int, cost: Long): SpannedString =
        buildSpannedString {
            append(getString(R.string.course_complete_subtitle_progress_part_1))
            bold {
                append(
                    getString(
                        R.string.course_complete_subtitle_progress_part_2,
                        resources.getQuantityString(R.plurals.points, score, score),
                        cost
                    )
                )
            }
            append(getString(R.string.course_complete_subtitle_progress_part_3, progress))
        }

    private fun getCertificateIssuedText(course: Course): String =
        if (course.certificateDistinctionThreshold == 0L) {
            getString(
                R.string.course_complete_subtitle_without_distinction_issued,
                resources.getQuantityString(R.plurals.points, course.certificateRegularThreshold.toInt(), course.certificateRegularThreshold)
            )
        } else {
            getString(
                R.string.course_complete_subtitle_certificate_issued,
                resources.getQuantityString(R.plurals.points, course.certificateRegularThreshold.toInt(), course.certificateRegularThreshold),
                course.certificateDistinctionThreshold
            )
        }

    private fun getFeedbackText(neededPoints: Long): SpannedString =
        buildSpannedString {
            bold {
                append(
                    getString(
                        R.string.course_complete_certificate_feedback,
                        resources.getQuantityString(R.plurals.points, neededPoints.toInt(), neededPoints)
                    )
                )
            }
        }

    private fun getCertificateDistinction(currentScore: Long, certificateDistinctionThreshold: Long): SpannedString =
        if (certificateDistinctionThreshold == 0L) {
            SpannedString("")
        } else {
            val neededScore = certificateDistinctionThreshold - currentScore
            buildSpannedString {
                append(
                    getString(
                        R.string.course_complete_subtitle_distinction_need_score_part_1,
                        resources.getQuantityString(
                            R.plurals.points,
                            certificateDistinctionThreshold.toInt(),
                            certificateDistinctionThreshold
                        )
                    )
                )
                bold { append(neededScore.toString()) }
                append(getString(R.string.course_complete_subtitle_distinction_need_score_part_2))
                append(".")
            }
        }

    interface Callback {
        fun showErrorMessage()
    }
}