package org.stepik.android.view.comment.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.empty_comments.view.*
import kotlinx.android.synthetic.main.error_no_connection.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.setOnPaginationListener
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.CommentsPresenter
import org.stepik.android.presentation.comment.CommentsView
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.comment.model.DiscussionOrderItem
import org.stepik.android.view.comment.ui.adapter.decorator.CommentItemDecoration
import org.stepik.android.view.comment.ui.adapter.delegate.CommentDataAdapterDelegate
import org.stepik.android.view.comment.ui.adapter.delegate.CommentLoadMoreRepliesAdapterDelegate
import org.stepik.android.view.comment.ui.adapter.delegate.CommentPlaceholderAdapterDelegate
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import org.stepik.android.view.comment.ui.dialog.RemoveCommentDialogFragment
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CommentsActivity :
    FragmentActivityBase(),
    CommentsView,
    ComposeCommentDialogFragment.Callback,
    RemoveCommentDialogFragment.Callback {
    companion object {
        private const val EXTRA_DISCUSSION_THREAD = "discussion_thread"
        private const val EXTRA_DISCUSSION_ID = "discussion_id"
        private const val EXTRA_STEP = "step"
        private const val EXTRA_IS_NEED_OPEN_COMPOSE = "is_need_open_compose"

        /**
         * [discussionId] - discussion id from deep link
         */
        fun createIntent(
            context: Context,
            step: Step,
            discussionThread: DiscussionThread,
            discussionId: Long? = null,
            isNeedOpenCompose: Boolean = false
        ): Intent =
            Intent(context, CommentsActivity::class.java)
                .putExtra(EXTRA_STEP, step)
                .putExtra(EXTRA_DISCUSSION_THREAD, discussionThread)
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

    private val step by lazy { intent.getParcelableExtra<Step>(EXTRA_STEP) }
    private val discussionThread by lazy { intent.getParcelableExtra<DiscussionThread>(EXTRA_DISCUSSION_THREAD) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_comments)

        injectComponent()
        commentsPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(CommentsPresenter::class.java)

        initCenteredToolbar(
            titleRes =
                if (discussionThread.thread == DiscussionThread.THREAD_SOLUTIONS) {
                    R.string.solutions_title
                } else {
                    R.string.comments_title
                },
            showHomeButton = true
        )
        centeredToolbar.overflowIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_comments_ordering)

        commentsAdapter = DefaultDelegateAdapter()
        commentsAdapter += CommentPlaceholderAdapterDelegate()
        commentsAdapter += CommentDataAdapterDelegate(
            actionListener = object : CommentDataAdapterDelegate.ActionListener {
                override fun onReplyClicked(parentCommentId: Long) {
                    showCommentComposeDialog(step, parent = parentCommentId)
                }

                override fun onVoteClicked(commentDataItem: CommentItem.Data, voteValue: Vote.Value) {
                    commentsPresenter.onChangeVote(commentDataItem, voteValue)
                }

                override fun onEditCommentClicked(commentDataItem: CommentItem.Data) {
                    showCommentComposeDialog(step, commentDataItem.comment.parent, commentDataItem.comment, commentDataItem.solution?.submission)
                }

                override fun onRemoveCommentClicked(commentDataItem: CommentItem.Data) {
                    showRemoveCommentDialog(commentDataItem.id)
                }

                override fun onProfileClicked(commentDataItem: CommentItem.Data) {
                    screenManager.openProfile(this@CommentsActivity, commentDataItem.comment.user ?: return)
                }

                override fun onSolutionClicked(discussionId: Long, solution: CommentItem.Data.Solution) {
                    showSolutionDialog(discussionId, solution)
                }
            }
        )
        commentsAdapter += CommentLoadMoreRepliesAdapterDelegate(commentsPresenter::onLoadMoreReplies)

        with(commentsRecycler) {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(CommentItemDecoration(
                separatorColor = ContextCompat.getColor(context, R.color.grey04),
                bigSeparatorBounds =
                    CommentItemDecoration.SeparatorBounds(
                        size = resources.getDimensionPixelSize(R.dimen.comment_item_separator_big),
                        offset = 0
                    ),
                smallSeparatorBounds =
                    CommentItemDecoration.SeparatorBounds(
                        size = resources.getDimensionPixelSize(R.dimen.comment_item_separator_small),
                        offset = resources.getDimensionPixelOffset(R.dimen.comment_item_reply_separator_offset)
                    )
            ))

            setOnPaginationListener(commentsPresenter::onLoadMore)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && composeCommentButton.isVisible) {
                        composeCommentButton.hide()
                    } else if (dy < 0 && !composeCommentButton.isVisible) {
                        composeCommentButton.show()
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

        if (discussionThread.thread == DiscussionThread.THREAD_SOLUTIONS) {
            emptyComments.placeholderMessage.setText(R.string.step_solutions_empty)
        }

        composeCommentButton.setOnClickListener { showCommentComposeDialog(step) }
        commentsSwipeRefresh.setOnRefreshListener { setDataToPresenter(forceUpdate = true) }
    }

    private fun injectComponent() {
        App.component()
            .commentsComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        val discussionId = intent.getLongExtra(EXTRA_DISCUSSION_ID, -1)
            .takeIf { it != -1L }

        if (intent.getBooleanExtra(EXTRA_IS_NEED_OPEN_COMPOSE, false)) {
            showCommentComposeDialog(step)
            intent.removeExtra(EXTRA_IS_NEED_OPEN_COMPOSE)
        }

        commentsPresenter.onDiscussion(discussionThread.discussionProxy, discussionId, forceUpdate)
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

    private fun showCommentComposeDialog(step: Step, parent: Long? = null, comment: Comment? = null, submission: Submission? = null) {
        analytic.reportEvent(Analytic.Screens.OPEN_WRITE_COMMENT)

        ComposeCommentDialogFragment
            .newInstance(discussionThread, step, parent, comment, submission)
            .showIfNotExists(supportFragmentManager, ComposeCommentDialogFragment.TAG)
    }

    private fun showRemoveCommentDialog(commentId: Long) {
        analytic.reportEvent(Analytic.Interaction.DELETE_COMMENT_TRIAL)

        RemoveCommentDialogFragment
            .newInstance(commentId)
            .showIfNotExists(supportFragmentManager, RemoveCommentDialogFragment.TAG)
    }

    private fun showSolutionDialog(discussionId: Long, solution: CommentItem.Data.Solution) {
        SolutionCommentDialogFragment
            .newInstance(intent.getParcelableExtra(EXTRA_STEP), solution.attempt, solution.submission, discussionThread, discussionId)
            .showIfNotExists(supportFragmentManager, SolutionCommentDialogFragment.TAG)
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
        root.snackbar(messageRes = R.string.no_connection)
    }

    override fun onCommentReplaced(commentsData: CommentsData, isCommentCreated: Boolean) {
        if (isCommentCreated) {
            commentsPresenter.onCommentCreated(commentsData)
        } else {
            commentsPresenter.onCommentUpdated(commentsData)
        }
    }

    override fun onDeleteComment(commentId: Long) {
        analytic.reportEvent(Analytic.Comments.DELETE_COMMENT_CONFIRMATION)
        commentsPresenter.removeComment(commentId)
    }
}