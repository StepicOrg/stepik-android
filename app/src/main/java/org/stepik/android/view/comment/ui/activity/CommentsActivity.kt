package org.stepik.android.view.comment.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.activity_profile_edit_password.*
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.error_no_connection.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepik.android.view.comment.ui.dialog.DeleteCommentDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.base.PaginationDirection
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.CommentsPresenter
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.comment.model.DiscussionOrderItem
import org.stepik.android.view.comment.ui.adapter.delegate.CommentDataAdapterDelegate
import org.stepik.android.view.comment.ui.adapter.delegate.CommentLoadMoreRepliesAdapterDelegate
import org.stepik.android.view.comment.ui.adapter.delegate.CommentPlaceholderAdapterDelegate
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import javax.inject.Inject

class CommentsActivity :
    FragmentActivityBase(),
    CommentsView,
    ComposeCommentDialogFragment.Callback,
    DeleteCommentDialogFragment.Callback {
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
    private lateinit var commentsViewStateDelegate: ViewStateDelegate<CommentsView.CommentsState>
    private lateinit var commentsAdapter: DefaultDelegateAdapter<CommentItem>

    private var isMenuOrderGroupVisible: Boolean = false
    private var menuDiscussionOrderItem: DiscussionOrderItem = DiscussionOrderItem.LAST_DISCUSSION

    private val commentPlaceholders = List(10) { CommentItem.Placeholder }

    private val stepId by lazy { intent.getLongExtra(EXTRA_STEP_ID, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_comments)

        injectComponent()
        commentsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CommentsPresenter::class.java)

        initCenteredToolbar(titleRes = R.string.comments_title, showHomeButton = true)
        centeredToolbar.overflowIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_comments_ordering)

        commentsAdapter = DefaultDelegateAdapter()
        commentsAdapter += CommentPlaceholderAdapterDelegate()
        commentsAdapter += CommentDataAdapterDelegate(
            actionListener = object : CommentDataAdapterDelegate.ActionListener {
                override fun onReplyClicked(parentCommentId: Long) {
                    showCommentComposeDialog(stepId, parent = parentCommentId)
                }

                override fun onVoteClicked(commentDataItem: CommentItem.Data, voteValue: Vote.Value) {
                    commentsPresenter.onChangeVote(commentDataItem, voteValue)
                }

                override fun onEditCommentClicked(commentDataItem: CommentItem.Data) {
                    showCommentComposeDialog(stepId, commentDataItem.comment.parent, commentDataItem.comment)
                }

                override fun onRemoveCommentClicked(commentDataItem: CommentItem.Data) {
                    showDeleteCommentDialog(commentDataItem.id)
                }
            }
        )
        commentsAdapter += CommentLoadMoreRepliesAdapterDelegate(commentsPresenter::onLoadMoreReplies)

        with(commentsRecycler) {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                ContextCompat.getDrawable(context, R.drawable.list_divider_h)?.let(::setDrawable)
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
                        ?: return

                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                    if (dy > 0) {
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount

                        if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                            post { commentsPresenter.onLoadMore(PaginationDirection.DOWN) }
                        }
                    } else {
                        if (pastVisibleItems == 0) {
                            post { commentsPresenter.onLoadMore(PaginationDirection.UP) }
                        }
                    }
                }
            })

            (itemAnimator as? SimpleItemAnimator)
                ?.supportsChangeAnimations = false
        }

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<CommentsView.State.Idle>()
        viewStateDelegate.addState<CommentsView.State.Loading>(commentsRecycler)
        viewStateDelegate.addState<CommentsView.State.NetworkError>(reportProblem)
        viewStateDelegate.addState<CommentsView.State.DiscussionLoaded>(commentsRecycler, emptyComments)

        commentsViewStateDelegate = ViewStateDelegate()
        commentsViewStateDelegate.addState<CommentsView.CommentsState.Loaded>(commentsRecycler)
        commentsViewStateDelegate.addState<CommentsView.CommentsState.Loading>(commentsRecycler)
        commentsViewStateDelegate.addState<CommentsView.CommentsState.EmptyComments>(emptyComments)

        setDataToPresenter()

        composeCommentButton.setOnClickListener { showCommentComposeDialog(stepId) }
        commentsSwipeRefresh.setOnRefreshListener { setDataToPresenter(forceUpdate = true) }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.coment_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.setGroupVisible(R.id.comments_menu_group, isMenuOrderGroupVisible)
        menu.findItem(menuDiscussionOrderItem.itemId)
            ?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                val discussionOrder =
                    DiscussionOrderItem
                        .getById(item.itemId)
                        ?.order

                if (discussionOrder != null) {
                    commentsPresenter.changeDiscussionOrder(discussionOrder)
                    true
                } else {
                    super.onOptionsItemSelected(item)
                }
            }
        }

    override fun setState(state: CommentsView.State) {
        commentsSwipeRefresh.isRefreshing = false
        commentsSwipeRefresh.isEnabled =
            state is CommentsView.State.NetworkError ||
            state is CommentsView.State.DiscussionLoaded

        viewStateDelegate.switchState(state)
        isMenuOrderGroupVisible = state is CommentsView.State.DiscussionLoaded

        when (state) {
            is CommentsView.State.Loading ->
                commentsAdapter.items = commentPlaceholders

            is CommentsView.State.DiscussionLoaded -> {
                commentsViewStateDelegate.switchState(state.commentsState)

                menuDiscussionOrderItem = DiscussionOrderItem.getBy(order = state.discussionOrder)

                when (state.commentsState) {
                    is CommentsView.CommentsState.Loading ->
                        commentsAdapter.items = commentPlaceholders

                    is CommentsView.CommentsState.Loaded ->
                        commentsAdapter.items = state.commentsState.commentItems
                }
            }
        }

        invalidateOptionsMenu()
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

    private fun showDeleteCommentDialog(commentId: Long) {
        val supportFragmentManager = supportFragmentManager
            ?.takeIf { it.findFragmentByTag(DeleteCommentDialogFragment.TAG) == null }
            ?: return

        analytic.reportEvent(Analytic.Screens.OPEN_WRITE_COMMENT)

        DeleteCommentDialogFragment
            .newInstance(commentId)
            .show(supportFragmentManager, DeleteCommentDialogFragment.TAG)
    }

    override fun focusDiscussion(discussionId: Long) {
        commentsRecycler.post {
            val itemIndex = commentsAdapter
                .items
                .indexOfFirst { it is CommentItem.Data && it.id == discussionId }

            if (itemIndex > 0) {
                commentsRecycler.layoutManager
                    ?.scrollToPosition(itemIndex)
            }
        }
    }

    override fun showNetworkError() {
        Snackbar
            .make(root, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    override fun onCommentReplaced(commentsData: CommentsData, isCommentCreated: Boolean) {
        if (isCommentCreated) {
//            commentsPresenter.o
        } else {
            commentsPresenter.onCommentUpdated(commentsData)
        }
    }

    override fun onDeleteComment(commentId: Long) {
        analytic.reportEvent(Analytic.Comments.DELETE_COMMENT_CONFIRMATION)
        commentsPresenter.removeComment(commentId)
    }
}