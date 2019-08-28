package org.stepik.android.view.comment.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.model.comments.Comment
import org.stepik.android.presentation.comment.CommentsPresenter
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import javax.inject.Inject

class CommentsActivity : FragmentActivityBase(), CommentsView {
    companion object {
        private const val EXTRA_DISCUSSION_PROXY = "discussion_proxy"
        private const val EXTRA_STEP_ID = "step_id"
        private const val EXTRA_IS_NEED_OPEN_COMPOSE = "is_need_open_compose"

        fun createIntent(context: Context, discussionProxy: String, stepId: Long, isNeedOpenCompose: Boolean = false): Intent =
            Intent(context, CommentsActivity::class.java)
                .putExtra(EXTRA_DISCUSSION_PROXY, discussionProxy)
                .putExtra(EXTRA_STEP_ID, stepId)
                .putExtra(EXTRA_IS_NEED_OPEN_COMPOSE, isNeedOpenCompose)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var commentsPresenter: CommentsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injectComponent()
        commentsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CommentsPresenter::class.java)

        setDataToPresenter()
    }

    private fun injectComponent() {
        App.component()
            .commentsComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        val discussionProxy = intent.getStringExtra(EXTRA_DISCUSSION_PROXY)
        val stepId = intent.getLongExtra(EXTRA_STEP_ID, -1)
        val isNeedOpenCompose = intent.getBooleanExtra(EXTRA_IS_NEED_OPEN_COMPOSE, false)


    }

    override fun onStart() {
        super.onStart()
        commentsPresenter.attachView(this)
    }

    override fun onStop() {
        commentsPresenter.detachView(this)
        super.onStop()
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