package org.stepik.android.view.comment.ui.dialog

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.dialog_compose_comment.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.argument
import org.stepic.droid.util.snackbar
import org.stepik.android.model.comments.Comment
import org.stepik.android.presentation.comment.ComposeCommentPresenter
import org.stepik.android.presentation.comment.ComposeCommentView
import javax.inject.Inject

class ComposeCommentDialogFragment : DialogFragment(), ComposeCommentView {
    companion object {
        private const val ARG_COMMENT = "comment"
        private const val ARG_PARENT = "parent"

        /**
         * [target] - comment target, e.g. step id
         * [parent] - parent comment id
         * [comment] - comment if comment should be edited
         */
        fun newInstance(target: Long, parent: Long?, comment: Comment): DialogFragment =
            ComposeCommentDialogFragment().apply {
                this.arguments = Bundle(3)
                    .also {
                        it.putLong(ARG_PARENT, parent ?: -1)
                        it.putParcelable(ARG_COMMENT, comment)
                    }
                this.target = target
            }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var composeCommentPresenter: ComposeCommentPresenter

    private var target: Long by argument()
    private val parent: Long? by lazy { arguments?.getLong(ARG_PARENT, -1)?.takeIf { it != -1L } }
    private val comment: Comment? by lazy { arguments?.getParcelable<Comment>(ARG_COMMENT) }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

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
        centeredToolbarTitle.setText(R.string.course_reviews_compose_title)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)
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
        }
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
            }

        composeCommentPresenter.attachView(this)
    }

    override fun onStop() {
        composeCommentPresenter.detachView(this)
        super.onStop()
    }

    private fun submit() {
        val oldComment = comment

        val text = commentEditText.text?.toString()

        if (oldComment == null) {
            val comment = Comment(
                target = target,
                parent = parent,
                text = text
            )
            composeCommentPresenter.createComment(comment)
        } else {
            val comment = oldComment.copy(text = text)
            composeCommentPresenter.updateComment(comment)
        }
    }

    override fun setState(state: ComposeCommentView.State) {
        when (state) {
            ComposeCommentView.State.Idle ->
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)

            ComposeCommentView.State.Loading ->
                ProgressHelper.activate(progressDialogFragment, childFragmentManager, LoadingProgressDialogFragment.TAG)

            is ComposeCommentView.State.Complete -> {
                ProgressHelper.dismiss(childFragmentManager, LoadingProgressDialogFragment.TAG)

                dismiss()
            }
        }
    }

    override fun showNetworkError() {
        snackbar(messageRes = R.string.connectionProblems)
    }
}