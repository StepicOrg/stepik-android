package org.stepik.android.view.submission.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_submissions.*
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.view_submissions_search_toolbar.*
import kotlinx.android.synthetic.main.view_subtitled_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.user.User
import org.stepik.android.presentation.submission.SubmissionsPresenter
import org.stepik.android.presentation.submission.SubmissionsView
import org.stepik.android.view.base.ui.extension.setTintList
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.step_quiz_review.routing.StepQuizReviewDeepLinkBuilder
import org.stepik.android.view.submission.routing.SubmissionDeepLinkBuilder
import org.stepik.android.view.submission.ui.adapter.delegate.SubmissionDataAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.core.model.PaginationDirection
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.setOnPaginationListener
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class SubmissionsDialogFragment :
    DialogFragment(),
    SubmissionsView,
    SubmissionsQueryFilterDialogFragment.Callback,
    InAppWebViewDialogFragment.Callback {
    companion object {
        const val TAG = "SubmissionsDialogFragment"

        private const val ARG_STATUS = "status"
        private const val ARG_REVIEW_INSTRUCTION = "review_instruction"

        fun newInstance(
            step: Step,
            isTeacher: Boolean = false,
            userId: Long = -1L,
            status: Submission.Status? = null,
            reviewInstruction: ReviewInstruction? = null,
            isSelectionEnabled: Boolean = false
        ): DialogFragment =
            SubmissionsDialogFragment()
                .apply {
                    this.step = step
                    this.isTeacher = isTeacher
                    this.userId = userId
                    this.isSelectionEnabled = isSelectionEnabled
                    this.arguments = Bundle(2)
                        .also {
                            it.putSerializable(ARG_STATUS, status)
                            it.putParcelable(ARG_REVIEW_INSTRUCTION, reviewInstruction)
                        }
                }
    }

    @Inject
    internal lateinit var userPreferences: UserPreferences

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    lateinit var submissionDeepLinkBuilder: SubmissionDeepLinkBuilder

    @Inject
    internal lateinit var stepQuizReviewDeepLinkBuilder: StepQuizReviewDeepLinkBuilder

    private var step: Step by argument()
    private var isTeacher: Boolean by argument()
    private var userId: Long by argument()
    private var isSelectionEnabled: Boolean by argument()
    private var status: Submission.Status? = null
    private var reviewInstruction: ReviewInstruction? = null

    private var submissionsFilterQuery = SubmissionsFilterQuery.DEFAULT_QUERY

    private val submissionsPresenter: SubmissionsPresenter by viewModels { viewModelFactory }

    private lateinit var submissionItemAdapter: DefaultDelegateAdapter<SubmissionItem>

    private lateinit var viewContentStateDelegate: ViewStateDelegate<SubmissionsView.ContentState>

    private val placeholders = List(10) { SubmissionItem.Placeholder }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)

        injectComponent()

        status = arguments?.getSerializable(ARG_STATUS) as? Submission.Status
        reviewInstruction = arguments?.getParcelable<ReviewInstruction>(ARG_REVIEW_INSTRUCTION)
        submissionsPresenter.fetchSubmissions(
            step,
            isTeacher,
            submissionsFilterQuery.copy(
                status = status?.scope,
                search = if (userId == -1L) null else resources.getString(R.string.submissions_user_filter, userId)
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_submissions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbar.isVisible = !isTeacher
        searchViewContainer.isVisible = isTeacher

        centeredToolbarTitle.setText(if (isSelectionEnabled) R.string.submissions_select_title else R.string.submissions_title)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setTintedNavigationIcon(R.drawable.ic_close_dark)

        AppCompatResources
            .getDrawable(requireContext(), R.drawable.ic_close_dark)
            ?.setTintList(requireContext(), R.attr.colorControlNormal)
            ?.let { backIcon.setImageDrawable(it) }
        backIcon.setOnClickListener { dismiss() }
        clearSearchButton.setOnClickListener {
            searchSubmissionsEditText.text?.clear()
            fetchSearchQuery()
        }
        filterIcon.setOnClickListener { submissionsPresenter.onFilterMenuItemClicked() }

        viewContentStateDelegate = ViewStateDelegate()
        viewContentStateDelegate.addState<SubmissionsView.ContentState.Idle>()
        viewContentStateDelegate.addState<SubmissionsView.ContentState.Loading>(swipeRefresh)
        viewContentStateDelegate.addState<SubmissionsView.ContentState.NetworkError>(error)
        viewContentStateDelegate.addState<SubmissionsView.ContentState.Content>(swipeRefresh)
        viewContentStateDelegate.addState<SubmissionsView.ContentState.ContentLoading>(swipeRefresh)
        viewContentStateDelegate.addState<SubmissionsView.ContentState.ContentEmpty>(report_empty)

        submissionItemAdapter = DefaultDelegateAdapter()
        submissionItemAdapter += SubmissionDataAdapterDelegate(
            currentUserId = userPreferences.userId,
            isTeacher = isTeacher,
            isSelectionEnabled = isSelectionEnabled,
            reviewInstruction = reviewInstruction,
            actionListener = object : SubmissionDataAdapterDelegate.ActionListener {
                override fun onSubmissionClicked(data: SubmissionItem.Data) {
                    showSolution(data)
                }

                override fun onUserClicked(user: User) {
                    screenManager.openProfile(requireContext(), user.id)
                }

                override fun onItemClicked(data: SubmissionItem.Data) {
                    (activity as? Callback
                        ?: parentFragment as? Callback
                        ?: targetFragment as? Callback)
                        ?.onSubmissionSelected(data.submission, data.attempt)
                    dismiss()
                }

                override fun onViewSubmissionsClicked(submissionDataItem: SubmissionItem.Data) {
                    val userIdQuery = resources.getString(R.string.submissions_user_filter, submissionDataItem.user.id)
                    searchSubmissionsEditText.setText(userIdQuery)
                    fetchSearchQuery()
                }

                override fun onSeeSubmissionReviewAction(submissionId: Long) {
                    val title = getString(R.string.comment_solution_pattern, submissionId)
                    val url = submissionDeepLinkBuilder.createSubmissionLink(step.id, submissionId)
                    openInWeb(title, url)
                }

                override fun onSeeReviewsReviewAction(session: Long) {
                    val title = getString(R.string.step_quiz_review_taken_title)
                    val url = stepQuizReviewDeepLinkBuilder.createTakenReviewDeepLink(session)
                    openInWeb(title, url)
                }
            }
        )
        submissionItemAdapter +=
            adapterDelegate<SubmissionItem, SubmissionItem.Placeholder>(R.layout.item_submission_placeholder)

        with(recycler) {
            adapter = submissionItemAdapter
            layoutManager = LinearLayoutManager(context)

            setOnPaginationListener { paginationDirection ->
                if (paginationDirection == PaginationDirection.NEXT) {
                    submissionsPresenter.fetchNextPage(step, isTeacher)
                }
            }

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.bg_submission_item_divider)?.let(::setDrawable)
            })
        }

        swipeRefresh.setOnRefreshListener { submissionsPresenter.fetchSubmissions(step, isTeacher, submissionsFilterQuery, forceUpdate = true) }
        tryAgain.setOnClickListener { submissionsPresenter.fetchSubmissions(step, isTeacher, submissionsFilterQuery, forceUpdate = true) }

        searchSubmissionsEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                fetchSearchQuery()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        searchSubmissionsEditText.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                clearSearchButton.isVisible = false
                searchSubmissionsEditText.setPadding(resources.getDimensionPixelSize(R.dimen.submissions_search_padding_left), 0, resources.getDimensionPixelSize(R.dimen.submissions_search_padding_without_text), 0)
            } else {
                clearSearchButton.isVisible = true
                searchSubmissionsEditText.setPadding(resources.getDimensionPixelSize(R.dimen.submissions_search_padding_left), 0, resources.getDimensionPixelSize(R.dimen.submissions_search_padding_with_text), 0)
            }
        }
        val userIdQuery = if (userId == -1L) null else resources.getString(R.string.submissions_user_filter, userId)
        userIdQuery?.let { searchSubmissionsEditText.setText(it) }
    }

    private fun injectComponent() {
        App.component()
            .submissionComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }

        submissionsPresenter.attachView(this)
    }

    override fun onStop() {
        submissionsPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: SubmissionsView.State) {
        swipeRefresh.isRefreshing = false
        if (state is SubmissionsView.State.Data) {
            viewContentStateDelegate.switchState(state.contentState)
            filterIcon.setImageResource(getFilterIcon(state.submissionsFilterQuery))
            submissionsFilterQuery = state.submissionsFilterQuery
            submissionItemAdapter.items =
                when (state.contentState) {
                    is SubmissionsView.ContentState.Loading ->
                        placeholders

                    is SubmissionsView.ContentState.Content ->
                        state.contentState.items

                    is SubmissionsView.ContentState.ContentLoading ->
                        state.contentState.items + SubmissionItem.Placeholder

                    else ->
                        emptyList()
                }
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun showSubmissionsFilterDialog(submissionsFilterQuery: SubmissionsFilterQuery) {
        SubmissionsQueryFilterDialogFragment
            .newInstance(submissionsFilterQuery, isPeerReview = step.actions?.doReview != null)
            .showIfNotExists(childFragmentManager, SubmissionsQueryFilterDialogFragment.TAG)
    }

    override fun onSyncFilterQueryWithParent(submissionsFilterQuery: SubmissionsFilterQuery) {
        submissionsPresenter.fetchSubmissions(step, isTeacher, submissionsFilterQuery, forceUpdate = true)
    }

    override fun onDismissed() {
        submissionsPresenter.fetchSubmissions(step, isTeacher, submissionsFilterQuery, forceUpdate = true)
    }

    private fun showSolution(submissionItem: SubmissionItem.Data) {
        SolutionCommentDialogFragment
            .newInstance(step, submissionItem.attempt, submissionItem.submission)
            .showIfNotExists(parentFragmentManager, SolutionCommentDialogFragment.TAG)
    }

    private fun getFilterIcon(updatedSubmissionQuery: SubmissionsFilterQuery): Int =
        if (updatedSubmissionQuery == SubmissionsFilterQuery.DEFAULT_QUERY.copy(search = updatedSubmissionQuery.search)) {
            R.drawable.ic_filter
        } else {
            R.drawable.ic_filter_active
        }

    private fun fetchSearchQuery() {
        searchSubmissionsEditText.hideKeyboard()
        searchSubmissionsEditText.clearFocus()
        submissionsPresenter.fetchSubmissions(
            step,
            isTeacher,
            submissionsFilterQuery.copy(search = searchSubmissionsEditText.text?.toString()),
            forceUpdate = true
        )
    }

    private fun openInWeb(title: String, url: String) {
        val dialog = InAppWebViewDialogFragment.newInstance(title, url, isProvideAuth = true)
        dialog.setTargetFragment(this, InAppWebViewDialogFragment.IN_APP_WEB_VIEW_DIALOG_REQUEST_CODE)
        dialog.showIfNotExists(parentFragmentManager, InAppWebViewDialogFragment.TAG)
    }

    interface Callback {
        fun onSubmissionSelected(submission: Submission, attempt: Attempt)
    }
}