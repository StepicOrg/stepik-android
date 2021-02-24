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
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.user.User
import org.stepik.android.presentation.submission.SubmissionsPresenter
import org.stepik.android.presentation.submission.SubmissionsView
import org.stepik.android.view.base.ui.extension.setTintList
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import org.stepik.android.view.submission.ui.adapter.delegate.SubmissionDataAdapterDelegate
import org.stepik.android.view.submission.ui.adapter.delegate.SubmissionPlaceholderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class SubmissionsDialogFragment : DialogFragment(), SubmissionsView, SubmissionsQueryFilterDialogFragment.Callback {
    companion object {
        const val TAG = "SubmissionsDialogFragment"

        private const val ARG_STATUS = "status"

        fun newInstance(
            step: Step,
            isTeacher: Boolean = false,
            status: Submission.Status? = null,
            isSelectionEnabled: Boolean = false
        ): DialogFragment =
            SubmissionsDialogFragment()
                .apply {
                    this.step = step
                    this.isTeacher = isTeacher
                    this.isSelectionEnabled = isSelectionEnabled
                    this.arguments?.putSerializable(ARG_STATUS, status)
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var step: Step by argument()
    private var isTeacher: Boolean by argument()
    private var isSelectionEnabled: Boolean by argument()
    private var status: Submission.Status? = null

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
        submissionsPresenter.fetchSubmissions(step.id, isTeacher, submissionsFilterQuery.copy(status = status?.scope))
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
            isItemClickEnabled = isSelectionEnabled,
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
            }
        )
        submissionItemAdapter += SubmissionPlaceholderAdapterDelegate()

        with(recycler) {
            adapter = submissionItemAdapter
            layoutManager = LinearLayoutManager(context)

            setOnPaginationListener { paginationDirection ->
                if (paginationDirection == PaginationDirection.NEXT) {
                    submissionsPresenter.fetchNextPage(step.id, isTeacher)
                }
            }

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }

        swipeRefresh.setOnRefreshListener { submissionsPresenter.fetchSubmissions(step.id, isTeacher, submissionsFilterQuery.copy(status = status?.scope), forceUpdate = true) }
        tryAgain.setOnClickListener { submissionsPresenter.fetchSubmissions(step.id, isTeacher, submissionsFilterQuery.copy(status = status?.scope), forceUpdate = true) }

        searchSubmissionsEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchSubmissionsEditText.hideKeyboard()
                searchSubmissionsEditText.clearFocus()
                submissionsPresenter.fetchSubmissions(
                    step.id,
                    isTeacher,
                    submissionsFilterQuery.copy(search = searchSubmissionsEditText.text?.toString()),
                    forceUpdate = true
                )
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
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
        submissionsPresenter.fetchSubmissions(step.id, isTeacher, submissionsFilterQuery, forceUpdate = true)
    }

    private fun showSolution(submissionItem: SubmissionItem.Data) {
        SolutionCommentDialogFragment
            .newInstance(step, submissionItem.attempt, submissionItem.submission)
            .showIfNotExists(parentFragmentManager, SolutionCommentDialogFragment.TAG)
    }

    interface Callback {
        fun onSubmissionSelected(submission: Submission, attempt: Attempt)
    }
}