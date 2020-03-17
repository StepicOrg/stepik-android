package org.stepik.android.view.submission.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_submissions.*
import kotlinx.android.synthetic.main.empty_default.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.user.User
import org.stepik.android.presentation.submission.SubmissionsPresenter
import org.stepik.android.presentation.submission.SubmissionsView
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import org.stepik.android.view.submission.ui.adapter.delegate.SubmissionDataAdapterDelegate
import org.stepik.android.view.submission.ui.adapter.delegate.SubmissionPlaceholderAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class SubmissionsDialogFragment : DialogFragment(), SubmissionsView {
    companion object {
        const val TAG = "SubmissionsDialogFragment"

        fun newInstance(step: Step, isSelectionEnabled: Boolean = false): DialogFragment =
            SubmissionsDialogFragment()
                .apply {
                    this.step = step
                    this.isSelectionEnabled = isSelectionEnabled
                }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var screenManager: ScreenManager

    private var step: Step by argument()
    private var isSelectionEnabled: Boolean by argument()

    private lateinit var submissionsPresenter: SubmissionsPresenter

    private lateinit var submissionItemAdapter: DefaultDelegateAdapter<SubmissionItem>

    private lateinit var viewStateDelegate: ViewStateDelegate<SubmissionsView.State>

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
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_FullScreenDialog)

        injectComponent()

        submissionsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(SubmissionsPresenter::class.java)
        submissionsPresenter.fetchSubmissions(step.id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_submissions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        centeredToolbarTitle.setText(if (isSelectionEnabled) R.string.submissions_select_title else R.string.submissions_title)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<SubmissionsView.State.Idle>()
        viewStateDelegate.addState<SubmissionsView.State.Loading>(swipeRefresh)
        viewStateDelegate.addState<SubmissionsView.State.NetworkError>(error)
        viewStateDelegate.addState<SubmissionsView.State.Content>(swipeRefresh)
        viewStateDelegate.addState<SubmissionsView.State.ContentLoading>(swipeRefresh)
        viewStateDelegate.addState<SubmissionsView.State.ContentEmpty>(report_empty)

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
                        ?.onSubmissionSelected(data.submission)
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
                    submissionsPresenter.fetchNextPage(step.id)
                }
            }

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })
        }

        swipeRefresh.setOnRefreshListener { submissionsPresenter.fetchSubmissions(step.id, forceUpdate = true) }
        tryAgain.setOnClickListener { submissionsPresenter.fetchSubmissions(step.id, forceUpdate = true) }
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
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
            }

        submissionsPresenter.attachView(this)
    }

    override fun onStop() {
        submissionsPresenter.detachView(this)
        super.onStop()
    }

    override fun setState(state: SubmissionsView.State) {
        swipeRefresh.isRefreshing = false

        viewStateDelegate.switchState(state)
        submissionItemAdapter.items =
            when (state) {
                is SubmissionsView.State.Loading ->
                    placeholders

                is SubmissionsView.State.Content ->
                    state.items

                is SubmissionsView.State.ContentLoading ->
                    state.items + SubmissionItem.Placeholder

                else ->
                    emptyList()
            }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    private fun showSolution(submissionItem: SubmissionItem.Data) {
        SolutionCommentDialogFragment
            .newInstance(step, submissionItem.attempt, submissionItem.submission)
            .showIfNotExists(fragmentManager ?: return, SolutionCommentDialogFragment.TAG)
    }

    interface Callback {
        fun onSubmissionSelected(submission: Submission)
    }
}