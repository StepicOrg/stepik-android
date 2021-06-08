package org.stepik.android.view.course_complete.ui.dialog

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Spanned
import android.text.SpannedString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
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
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepik.android.domain.course.analytic.CourseViewSource
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

    private var course: Course by argument()
    private val viewModel: CourseCompleteViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var viewStateDelegate: ViewStateDelegate<CourseCompleteFeature.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TopCornersRoundedBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_course_complete, container, false)

    private fun injectComponent() {
        App.component()
            .courseCompleteComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        viewStateDelegate.addState<CourseCompleteFeature.State.NetworkError>()
        viewModel.onNewMessage(CourseCompleteFeature.Message.Init(course))
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
                dismiss()
            } else {
                setupDialogView(dialogViewInfo)
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
                    isSuccess = false,
                    primaryActionStringRes = R.string.course_complete_action_find_new_course,
                    secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
                )
            }
            progress < 20f && courseCompleteInfo.course.hasCertificate -> {
                setupNotReceivedCertificate(
                    courseCompleteInfo = courseCompleteInfo,
                    headerImage = R.drawable.ic_tak_demo_lesson,
                    isSuccess = false,
                    primaryActionStringRes = R.string.course_complete_action_find_new_course,
                    secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
                )
            }
            progress >= 20f && progress < 80f && !courseCompleteInfo.course.hasCertificate -> {
                setupCertificateNotIssued(
                    courseCompleteInfo = courseCompleteInfo,
                    headerImage = R.drawable.ic_tak_neutral,
                    isSuccess = true,
                    primaryActionStringRes = R.string.course_complete_action_find_new_course,
                    secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
                )
            }
            progress >= 20f && progress < 80f && courseCompleteInfo.course.hasCertificate -> {
                val courseScore = score.toLong()
                when {
                    courseScore < courseCompleteInfo.course.certificateRegularThreshold -> {
                        setupNotReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_neutral,
                            isSuccess = true,
                            primaryActionStringRes = R.string.course_complete_action_find_new_course,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
                        )
                    }
                    courseScore > courseCompleteInfo.course.certificateRegularThreshold && (courseScore < courseCompleteInfo.course.certificateDistinctionThreshold || courseCompleteInfo.course.certificateDistinctionThreshold == 0L)  -> {
                        val distinctionSubtitle = getCertificateDistinction(score.toLong(), courseCompleteInfo.course.certificateDistinctionThreshold)
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_regular_certificate,
                            distinctionSubtitle = distinctionSubtitle,
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
                        )
                    }
                    courseScore > courseCompleteInfo.course.certificateDistinctionThreshold -> {
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_distinction_certificate,
                            distinctionSubtitle = SpannedString(""),
                            primaryActionStringRes = -1,
                            secondaryActionStringRes = R.string.course_complete_action_back_to_assigments
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
                    isSuccess = false,
                    primaryActionStringRes = primaryAction,
                    secondaryActionStringRes = secondaryAction
                )
            }

            progress >= 80f && courseCompleteInfo.course.hasCertificate -> {
                val courseScore = score.toLong()
                when {
                    courseScore < courseCompleteInfo.course.certificateRegularThreshold -> {
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_back_to_assigments
                        } else {
                            R.string.course_complete_action_leave_review to R.string.course_complete_action_back_to_assigments
                        }
                        setupNotReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_neutral,
                            isSuccess = true,
                            primaryActionStringRes = primaryAction,
                            secondaryActionStringRes = secondaryAction
                        )
                    }
                    courseScore >= courseCompleteInfo.course.certificateRegularThreshold && (courseScore < courseCompleteInfo.course.certificateDistinctionThreshold || courseCompleteInfo.course.certificateDistinctionThreshold == 0L) -> {
                        val distinctionSubtitle = getCertificateDistinction(score.toLong(), courseCompleteInfo.course.certificateDistinctionThreshold)
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_back_to_assigments
                        } else {
                            R.string.course_complete_action_leave_review to R.string.course_complete_action_back_to_assigments
                        }
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_regular_certificate,
                            distinctionSubtitle = distinctionSubtitle,
                            primaryActionStringRes = primaryAction,
                            secondaryActionStringRes = secondaryAction
                        )
                    }
                    courseScore > courseCompleteInfo.course.certificateDistinctionThreshold -> {
                        val (primaryAction, secondaryAction) = if (courseCompleteInfo.hasReview) {
                            -1 to R.string.course_complete_action_find_new_course
                        } else {
                            R.string.course_complete_action_find_new_course to R.string.course_complete_action_leave_review
                        }
                        setupReceivedCertificate(
                            courseCompleteInfo = courseCompleteInfo,
                            headerImage = R.drawable.ic_tak_distinction_certificate,
                            SpannedString(""),
                            primaryAction,
                            secondaryAction
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
            }
        }
        return CourseCompleteDialogViewInfo(
            headerImage,
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
    private fun setupDialogView(courseCompleteDialogViewInfo: CourseCompleteDialogViewInfo) {
        courseCompleteLogo.setImageResource(courseCompleteDialogViewInfo.headerImage)
        courseCompleteTitle.text = courseCompleteDialogViewInfo.title
        courseCompleteFeedback.text = courseCompleteDialogViewInfo.feedbackText
        courseCompleteFeedback.isVisible = courseCompleteDialogViewInfo.feedbackText.isNotEmpty()
        courseCompleteSubtitle.text = courseCompleteDialogViewInfo.subtitle

        viewCertificateAction.isVisible = courseCompleteDialogViewInfo.isViewCertificateVisible
        shareResultAction.isVisible = courseCompleteDialogViewInfo.isShareVisible

        if (courseCompleteDialogViewInfo.primaryActionStringRes != -1) {
            primaryAction.isVisible = true
            primaryAction.setText(courseCompleteDialogViewInfo.primaryActionStringRes)
            setupOnActionClickListener(
                courseCompleteDialogViewInfo.primaryActionStringRes,
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
                secondaryAction
            )
        } else {
            secondaryAction.isVisible = false
        }

        courseCompleteDivider.isVisible = primaryAction.isVisible || secondaryAction.isVisible
    }

    private fun setupOnActionClickListener(actionStringRes: Int, actionButton: MaterialButton) {
        actionButton.setOnClickListener {
            when (actionStringRes) {
                R.string.course_complete_action_back_to_assigments -> {
                    screenManager.showCourseFromNavigationDialog(requireContext(), course.id, CourseViewSource.Unknown, CourseScreenTab.SYLLABUS, false)
                }
                R.string.course_complete_action_find_new_course -> {
                    screenManager.showMainFeed(requireActivity(), MainFeedActivity.CATALOG_INDEX)
                }
                R.string.course_complete_action_leave_review -> {
                    screenManager.showCourseFromNavigationDialog(requireContext(), course.id, CourseViewSource.Unknown, CourseScreenTab.REVIEWS, false)
                }
            }
        }
    }

    /**
     * Setup functions for ui elements
     */
    private fun getSubtitleTextBeginning(score: Int, progress: Int, cost: Long): Spanned =
        HtmlCompat.fromHtml(
            getString(
                R.string.course_complete_subtitle_progress,
                resources.getQuantityString(
                    R.plurals.points,
                    score,
                    score
                ),
                cost,
                progress
            ),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )

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
                getString(
                    R.string.course_complete_subtitle_distinction_need_score,
                    resources.getQuantityString(R.plurals.points, certificateDistinctionThreshold.toInt(), certificateDistinctionThreshold),
                    neededScore
                )
            }
        }
}