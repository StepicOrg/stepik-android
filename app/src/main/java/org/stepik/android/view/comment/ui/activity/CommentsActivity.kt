package org.stepik.android.view.comment.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.error_no_connection.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.model.comments.Comment
import org.stepik.android.presentation.comment.CommentsPresenter
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CommentsActivity : FragmentActivityBase(), CommentsView {
    companion object {
        private const val EXTRA_DISCUSSION_PROXY = "discussion_proxy"
        private const val EXTRA_DISCUSSION_ID = "discussion_id"
        private const val EXTRA_STEP_ID = "step_id"
        private const val EXTRA_IS_NEED_OPEN_COMPOSE = "is_need_open_compose"

        /**
         * [discussionId] - discussion id from deep link
         */
        fun createIntent(
            context: Context,
            stepId: Long,
            discussionProxy: String,
            discussionId: Long? = null,
            isNeedOpenCompose: Boolean = false
        ): Intent =
            Intent(context, CommentsActivity::class.java)
                .putExtra(EXTRA_STEP_ID, stepId)
                .putExtra(EXTRA_DISCUSSION_PROXY, discussionProxy)
                .putExtra(EXTRA_DISCUSSION_ID, discussionId ?: -1)
                .putExtra(EXTRA_IS_NEED_OPEN_COMPOSE, isNeedOpenCompose)

    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var commentsPresenter: CommentsPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<CommentsView.State>

    private val stepId by lazy { intent.getLongExtra(EXTRA_STEP_ID, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_comments)
        initCenteredToolbar(titleRes = R.string.comments_title, showHomeButton = true)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CommentsView.State.Idle>()
        viewStateDelegate.addState<CommentsView.State.Loading>(commentsSwipeRefresh)
        viewStateDelegate.addState<CommentsView.State.NetworkError>(reportProblem)
        viewStateDelegate.addState<CommentsView.State.DiscussionLoaded>(commentsSwipeRefresh)

        injectComponent()
        commentsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CommentsPresenter::class.java)

        setDataToPresenter()

        composeCommentButton.setOnClickListener { showCommentComposeDialog(stepId) }
    }

    private fun injectComponent() {
        App.component()
            .commentsComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        val discussionProxy = intent.getStringExtra(EXTRA_DISCUSSION_PROXY)
        val discussionId = intent.getLongExtra(EXTRA_DISCUSSION_ID, -1)
            .takeIf { it != -1L }

        if (intent.getBooleanExtra(EXTRA_IS_NEED_OPEN_COMPOSE, false)) {
            showCommentComposeDialog(stepId)
            intent.removeExtra(EXTRA_IS_NEED_OPEN_COMPOSE)
        }

        commentsPresenter.onDiscussion(discussionProxy, discussionId, forceUpdate)
    }

    override fun onStart() {
        super.onStart()
        commentsPresenter.attachView(this)
    }

    override fun onStop() {
        commentsPresenter.detachView(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun setState(state: CommentsView.State) {

    }

    private fun showCommentComposeDialog(stepId: Long, parent: Long? = null, comment: Comment? = null) {
        val supportFragmentManager = supportFragmentManager
            ?.takeIf { it.findFragmentByTag(ComposeCommentDialogFragment.TAG) == null }
            ?: return

        analytic.reportEvent(Analytic.Screens.OPEN_WRITE_COMMENT)

        ComposeCommentDialogFragment
            .newInstance(target = stepId, parent = parent, comment = comment)
            .show(supportFragmentManager, ComposeCommentDialogFragment.TAG)
    }
}