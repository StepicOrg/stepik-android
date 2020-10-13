package org.stepik.android.view.comment.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.dialog_compose_comment.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.DiscardTextDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.setTintedNavigationIcon
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.comment.ComposeCommentPresenter
import org.stepik.android.presentation.comment.ComposeCommentView
import org.stepik.android.view.submission.ui.delegate.setSubmission
import org.stepik.android.view.submission.ui.dialog.SubmissionsDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class ComposeCommentDialogFragment :
    DialogFragment(),
    ComposeCommentView,
    DiscardTextDialogFragment.Callback,
    SubmissionsDialogFragment.Callback {

    companion object {
        const val TAG = "ComposeCommentDialogFragment"

        private const val ARG_COMMENT = "comment"
        private const val ARG_PARENT = "parent"
        private const val ARG_SUBMISSION = "submission"

        /**
         * [discussionThread] - current discussion thread
         * [step] - comment target, e.g. step id
         * [parent] - parent comment id
         * [comment] - comment if comment should be edited
         */
        fun newInstance(discussionThread: DiscussionThread, step: Step, parent: Long?, comment: Comment?, submission: Submission?): DialogFragment =
            ComposeCommentDialogFragment().apply {
                this.arguments = Bundle(4)
                    .also {
                        it.putLong(ARG_PARENT, parent ?: -1)
                        it.putParcelable(ARG_COMMENT, comment)
                        it.putParcelable(ARG_SUBMISSION, submission)
                    }
                this.discussionThread = discussionThread
                this.step = step
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var composeCommentPresenter: ComposeCommentPresenter

    private var discussionThread: DiscussionThread by argument()
    private var step: Step by argument()
    private val parent: Long? by lazy { arguments?.getLong(ARG_PARENT, -1)?.takeIf { it != -1L } }
    private val comment: Comment? by lazy { arguments?.getParcelable<Comment>(ARG_COMMENT) }
    private val submission: Submission? by lazy { arguments?.getParcelable<Submission>(ARG_SUBMISSION) }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private lateinit var viewStateDelegate: ViewStateDelegate<ComposeCommentView.State>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                onClose()
            }
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)

        injectComponent()
        composeCommentPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(ComposeCommentPresenter::class.java)
    }

    private fun injectComponent() {
        App.component()
            .composeCommentComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_compose_comment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<ComposeCommentView.State.Idle>(commentContent)
        viewStateDelegate.addState<ComposeCommentView.State.Loading>(commentContent)
        viewStateDelegate.addState<ComposeCommentView.State.NetworkError>(error)
        viewStateDelegate.addState<ComposeCommentView.State.Create>(commentContent, commentSolution)
        viewStateDelegate.addState<ComposeCommentView.State.Complete>(commentContent)

        centeredToolbarTitle.setText(
            if (discussionThread.thread == DiscussionThread.THREAD_SOLUTIONS) R.string.solutions_compose_title else R.string.comment_compose_title)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setTintedNavigationIcon(R.drawable.ic_close_dark)
        centeredToolbar.inflateMenu(R.menu.comment_compose_menu)
        centeredToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.comment_submit) {
                submit()
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            commentEditText.setText(comment?.text)
            requestFocusNewComment(comment?.text ?: "")
        }
        invalidateMenuState()

        commentEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {
                invalidateMenuState()
            }
        })

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }

        commentSolution.setOnClickListener { showSubmissions() }
        setDataToPresenter()
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        composeCommentPresenter.onData(discussionThread, step.id, parent, submission, forceUpdate)
    }

    private fun invalidateMenuState() {
        centeredToolbar.menu.findItem(R.id.comment_submit)?.isEnabled =
            !commentEditText.text.isNullOrEmpty()
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
            }

        composeCommentPresenter.attachView(this)
    }

    override fun onStop() {
        composeCommentPresenter.detachView(this)
        super.onStop()
    }

    private fun showSubmissions() {
        SubmissionsDialogFragment
            .newInstance(step, isSelectionEnabled = true)
            .showIfNotExists(childFragmentManager, SubmissionsDialogFragment.TAG)
    }

    private fun submit() {
        commentEditText.hideKeyboard()
        val oldComment = comment

        val text = commentEditText.text?.toString()

        if (oldComment == null) {
            val comment = Comment(
                target = step.id,
                parent = parent,
                text = text,
                thread = discussionThread.thread
            )
            composeCommentPresenter.createComment(comment)
        } else {
            val comment = oldComment.copy(text = text)
            composeCommentPresenter.updateComment(comment)
        }
    }

    override fun setState(state: ComposeCommentView.State) {
        if (state is ComposeCommentView.State.Loading) {
            ProgressHelper.activate(progressDialogFragment, childFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)
        }

        when (state) {
            is ComposeCommentView.State.Create -> {
                commentSolution.isEnabled = comment == null
                commentSolution.setSubmission(state.submission, showArrow = commentSolution.isEnabled)
                commentSolutionSeparator.isVisible = state.submission != null
            }

            is ComposeCommentView.State.Complete -> {
                (activity as? Callback
                    ?: parentFragment as? Callback
                    ?: targetFragment as? Callback)
                    ?.onCommentReplaced(state.commentsData, state.isCommentCreated)

                super.dismiss()
            }
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun dismiss() {
        onClose()
    }

    private fun onClose() {
        if (commentEditText.text.isNullOrEmpty() || commentEditText.text.toString() == comment?.text) {
            super.dismiss()
        } else {
            if (childFragmentManager.findFragmentByTag(DiscardTextDialogFragment.TAG) == null) {
                DiscardTextDialogFragment
                    .newInstance()
                    .show(childFragmentManager, DiscardTextDialogFragment.TAG)
            }
        }
    }

    private fun requestFocusNewComment(text: String) {
        if (text.isNotEmpty()) return
        commentEditText.post {
            commentEditText.requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDiscardConfirmed() {
        super.dismiss()
    }

    override fun onSubmissionSelected(submission: Submission, attempt: Attempt) {
        composeCommentPresenter.onSubmissionSelected(submission)
    }

    interface Callback {
        fun onCommentReplaced(commentsData: CommentsData, isCommentCreated: Boolean)
    }
}