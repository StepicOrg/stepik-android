package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.error_no_connection.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.CommentManager
import org.stepic.droid.core.commentcount.contract.CommentCountPoster
import org.stepic.droid.core.comments.contract.CommentsListener
import org.stepic.droid.core.presenters.DiscussionPresenter
import org.stepic.droid.core.presenters.VotePresenter
import org.stepic.droid.core.presenters.contracts.DiscussionView
import org.stepic.droid.core.presenters.contracts.VoteView
import org.stepik.android.model.user.User
import org.stepic.droid.model.comments.*
import org.stepic.droid.ui.activities.NewCommentActivity
import org.stepic.droid.ui.adapters.CommentsAdapter
import org.stepic.droid.ui.dialogs.DeleteCommentDialogFragment
import org.stepic.droid.ui.util.ContextMenuRecyclerView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.*
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.model.comments.Vote
import java.util.*
import javax.inject.Inject

@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "false positive: commentManager is not null")
class CommentsFragment : FragmentBase(),
        SwipeRefreshLayout.OnRefreshListener,
        DiscussionView,
        CommentsListener,
        DeleteCommentDialogFragment.DialogCallback,
        VoteView {
    companion object {
        private val discussionIdKey = "dis_id_key"
        private val stepIdKey = "stepId"
        private val needInstaOpenKey = "needInstaOpenKey"

        private val replyMenuId = 100
        private val likeMenuId = 101
        private val unLikeMenuId = 102
        private val reportMenuId = 103
        private val cancelMenuId = 104
        private val deleteMenuId = 105
        private val copyTextMenuId = 106
        private val userMenuId = 107
        private val linksStartIndexId = 300 // inclusive
        var firstLinkShift = 0


        fun newInstance(discussionId: String, stepId: Long, needInstaOpen: Boolean): Fragment {
            val args = Bundle()
            args.putString(discussionIdKey, discussionId)
            args.putLong(stepIdKey, stepId)
            args.putBoolean(needInstaOpenKey, needInstaOpen)
            val fragment = CommentsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var commentManager: CommentManager

    lateinit var commentAdapter: CommentsAdapter

    lateinit var discussionId: String
    var stepId: Long? = null

    var needInsertOrUpdateLate: Comment? = null
    val links = ArrayList<String>()

    @Inject
    lateinit var discussionPresenter: DiscussionPresenter

    @Inject
    lateinit var commentsClient: Client<CommentsListener>

    @Inject
    lateinit var votePresenter: VotePresenter

    @Inject
    lateinit var commentCountPoster: CommentCountPoster


    override fun injectComponent() {
        stepId = arguments.getLong(stepIdKey)
        App
                .componentManager()
                .stepComponent(stepId!!)
                .commentsComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onReleaseComponent() {
        super.onReleaseComponent()
        App
                .componentManager()
                .releaseStepComponent(stepId!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentAdapter = CommentsAdapter(commentManager, context)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_comments, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discussionId = arguments.getString(discussionIdKey)
        setHasOptionsMenu(true)
        initToolbar()
        initSwipeRefreshLayout()
        initRecyclerView()
        initAddCommentButton()

        commentsClient.subscribe(this)
        discussionPresenter.attachView(this)
        votePresenter.attachView(this)


        //open form requested from caller
        val needInstaOpenForm = arguments.getBoolean(needInstaOpenKey)
        if (needInstaOpenForm) {
            screenManager.openNewCommentForm(this, stepId, null)
            arguments.putBoolean(needInstaOpenKey, false)
        }

        showEmptyProgressOnCenter()
        if (commentManager.isEmpty()) {
            loadDiscussionProxyById()
        } else {
            showEmptyProgressOnCenter(false)
        }
    }

    private fun initToolbar() {
        initCenteredToolbar(R.string.comments_title, true, getCloseIconDrawableRes())
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayoutComments.setOnRefreshListener(this)
    }

    private fun initAddCommentButton() {
        addNewCommentButton.setOnClickListener {
            stepId?.let {
                screenManager.openNewCommentForm(this, it, null)
            }
        }
    }

    private fun initRecyclerView() {
        recyclerViewComments.adapter = commentAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.isSmoothScrollbarEnabled = false
        recyclerViewComments.layoutManager = linearLayoutManager
        registerForContextMenu(recyclerViewComments)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle(R.string.one_comment_title)

        val info = menuInfo as? ContextMenuRecyclerView.RecyclerViewContextMenuInfo ?: return

        val position = info.position //resolve which should show
        val userId = userPreferences.userId
        val comment = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment

        if (userId > 0) {
            //it is not anonymous
            menu?.add(Menu.NONE, replyMenuId, Menu.NONE, R.string.reply_title)
        }

        links.clear()
        links.addAll(StringUtil.pullLinks(textResolver.fromHtml(comment.text).toString()))
        firstLinkShift = 0
        if (links.isNotEmpty()) {
            links.forEach {
                menu?.add(Menu.NONE, firstLinkShift + linksStartIndexId, Menu.NONE, it)
                firstLinkShift++
            }
        }

        if (userId > 0) {
            menu?.add(Menu.NONE, copyTextMenuId, Menu.NONE, R.string.copy_text_label)
            val commentUser = comment.user
            val commentVote = comment.vote
            if (commentUser != null && commentUser.toLong() != userId && commentVote != null) {
                //it is not current user and vote is available
                val vote = commentManager.getVoteByVoteId(commentVote)
                if (vote?.value != null && vote.value == Vote.Value.LIKE) {
                    //if we have like -> show suggest for unlike
                    menu?.add(Menu.NONE, unLikeMenuId, Menu.NONE, R.string.unlike_label)
                } else {
                    menu?.add(Menu.NONE, likeMenuId, Menu.NONE, R.string.like_label)
                }
                menu?.add(Menu.NONE, reportMenuId, Menu.NONE, R.string.report_label)
            }
        }

        val commentUser: User? = comment.user?.let { commentManager.getUserById(it) }

        val userNameText: String? = commentUser?.fullName
        if (userNameText?.isNotBlank() == true) {
            val spannableUserName = SpannableString(userNameText)
            spannableUserName.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.black)), 0, spannableUserName.length, 0)

            val userMenuItem = menu?.add(Menu.NONE, userMenuId, Menu.NONE, spannableUserName)
            userMenuItem?.titleCondensed = userNameText
        }

        if (userId > 0) {
            if (comment.actions?.delete == true) {
                val deleteText = getString(R.string.delete_label)
                val spannableString = SpannableString(deleteText)
                spannableString.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.feedback_bad_color)), 0, spannableString.length, 0)
                val deleteMenuItem = menu?.add(Menu.NONE, deleteMenuId, Menu.NONE, spannableString)
                deleteMenuItem?.titleCondensed = deleteText
            }
        }
        if (userId <= 0) {
            //todo: Cancel only for anonymous?
            menu?.add(Menu.NONE, cancelMenuId, Menu.NONE, R.string.cancel)
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val info = item?.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo
        when (item.itemId) {
            replyMenuId -> {
                replyToComment(info.position)
                return true
            }

            likeMenuId -> {
                likeComment(info.position)
                return true
            }

            unLikeMenuId -> {
                unlikeComment(info.position)
                return true
            }

            reportMenuId -> {
                abuseComment(info.position)
                return true
            }

            deleteMenuId -> {
                deleteComment(info.position)
                return true
            }

            copyTextMenuId -> {
                copyTextToClipBoard(info.position)
                return true
            }

            userMenuId -> {
                openUserProfile(info.position)
                return true
            }

            in linksStartIndexId until linksStartIndexId + firstLinkShift -> {
                val index = item.itemId
                clickLinkInComment(links[index - linksStartIndexId])
                return true
            }

            else -> return super.onContextItemSelected(item)
        }
    }

    private fun openUserProfile(position: Int) {
        if (position < 0 && position >= commentManager.getSize()) return

        val commentUser = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment.user ?: return
        val userId = commentManager.getUserById(commentUser)?.id
        if (userId != null) {
            analytic.reportEvent(Analytic.Profile.CLICK_USER_IN_COMMENT)
            screenManager.openProfile(activity, userId.toLong())
        }
    }

    private fun clickLinkInComment(link: String) {
        screenManager.openInWeb(activity, link)
    }

    private fun copyTextToClipBoard(position: Int) {
        val comment: Comment? = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        comment?.text?.let {

            val label = getString(R.string.copy_text_label)
            val plainText = textResolver.fromHtml(it)
            val clipData = if (Build.VERSION.SDK_INT > 16) {
                ClipData.newHtmlText(label, plainText, it)
            } else {
                ClipData.newPlainText(label, plainText)
            }

            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.primaryClip = clipData

            Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteComment(position: Int) {
        analytic.reportEvent(Analytic.Interaction.DELETE_COMMENT_TRIAL)
        val comment: Comment? = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        val commentId = comment?.id
        commentId?.let {
            val dialog = DeleteCommentDialogFragment.Companion.newInstance(it)
            dialog.setTargetFragment(this, 0)
            if (!dialog.isAdded) {
                dialog.show(fragmentManager, null)
            }
        }

    }

    private fun replyToComment(position: Int) {
        val comment: Comment? = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        comment?.let {
            screenManager.openNewCommentForm(this, stepId, it.parent ?: it.id)
        }
    }

    private fun likeComment(position: Int) {
        vote(position, Vote.Value.LIKE)
    }

    private fun unlikeComment(position: Int) {
        vote(position, null)
    }

    private fun abuseComment(position: Int) {
        vote(position, Vote.Value.DISLIKE)
    }

    private fun vote(position: Int, voteValue: Vote.Value?) {
        if (sharedPreferenceHelper.authResponseFromStore == null) {
            Toast.makeText(context, R.string.anonymous_like_mark_comment, Toast.LENGTH_SHORT).show()
            return
        }


        val comment = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        val voteId = comment.vote
        val commentId = comment.id
        voteId?.let {
            val voteObject = Vote(voteId, voteValue)
            votePresenter.doVote(voteObject, commentId)
        }

    }


    private fun loadDiscussionProxyById(id: String = discussionId) {
        discussionPresenter.loadDiscussion(id)
    }

    override fun onStop() {
        super.onStop()
        cancelSwipeRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        commentsClient.unsubscribe(this)
        discussionPresenter.detachView(this)
        votePresenter.detachView(this)

        swipeRefreshLayoutComments.setOnRefreshListener(null)
        addNewCommentButton.setOnClickListener(null)
        unregisterForContextMenu(recyclerViewComments)
    }

    override fun onRefresh() {
        commentManager.reset()
        loadDiscussionProxyById()
    }

    private fun showEmptyProgressOnCenter(needShow: Boolean = true) {
        if (needShow) {
            ProgressHelper.activate(loadProgressbarOnEmptyScreen)
            showEmptyState(false)
            showInternetConnectionProblem(false)
        } else {
            ProgressHelper.dismiss(loadProgressbarOnEmptyScreen)
        }
    }

    private fun showEmptyState(isNeedShow: Boolean = true) {
        if (isNeedShow) {
            emptyComments.visibility = View.VISIBLE
            showEmptyProgressOnCenter(false)
            showInternetConnectionProblem(false)
        } else {
            emptyComments.visibility = View.GONE
        }
    }

    private fun showInternetConnectionProblem(needShow: Boolean = true) {
        if (needShow) {
            reportProblem.visibility = View.VISIBLE
            showEmptyState(false)
            showEmptyProgressOnCenter(false)
        } else {
            reportProblem.visibility = View.GONE
        }
    }

    private fun cancelSwipeRefresh() {
        ProgressHelper.dismiss(swipeRefreshLayoutComments)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (!commentManager.isDiscussionProxyNull()) {
            inflater?.inflate(R.menu.coment_list_menu, menu)

            val defaultItem = menu?.findItem(sharedPreferenceHelper.discussionOrder.menuId)
            defaultItem?.isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        DiscussionOrder.values().forEach {
            if (it.menuId == item?.itemId) {
                sharedPreferenceHelper.discussionOrder = it
                item.isChecked = true

                commentManager.resetAll()
                commentAdapter.notifyDataSetChanged()


                showEmptyProgressOnCenter()
                commentManager.loadComments()
                //todo reload comments, set to comment manager, and resolve above by id from preferences.
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun onConnectionProblemBase() {
        cancelSwipeRefresh()
        if (commentManager.isEmpty()) {
            showInternetConnectionProblem()
        } else {
            Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
            commentManager.clearAllLoadings()
            commentAdapter.notifyDataSetChanged()
        }
    }

    //DiscussionView View:

    override fun onInternetProblemInComments() {
        onConnectionProblemBase()
    }

    override fun onEmptyComments(discussionProxy: DiscussionProxy) {
        cancelSwipeRefresh()
        if (!commentManager.isEmpty()) {
            commentManager.resetAll(discussionProxy)
            commentAdapter.notifyDataSetChanged()
        }
        showEmptyState()
    }

    override fun onLoaded(discussionProxy: DiscussionProxy) {
        commentManager.setDiscussionProxy(discussionProxy)
        activity?.invalidateOptionsMenu()
        commentManager.loadComments()
    }


    //CommentsListener

    override fun onCommentsLoaded() {
        cancelSwipeRefresh()
        showEmptyProgressOnCenter(false)
        showInternetConnectionProblem(false)
        if (!commentManager.isEmpty()) {
            showEmptyState(false)
        }
        val needInsertLocal: Comment? = needInsertOrUpdateLate
        if (needInsertLocal != null &&
                (!commentManager.isCommentCached(needInsertLocal.id) || (needInsertLocal.parent != null && !commentManager.isCommentCached(needInsertLocal.parent)))) {
            val longArr = listOfNotNull(needInsertLocal.id, needInsertLocal.parent).toLongArray()
            commentManager.loadCommentsByIds(longArr)
        } else {
            //we have only our comment.
            if (needInsertLocal != null) {
                commentManager.updateOnlyCommentsIfCachedSilent(listOf(needInsertLocal))
            }
            needInsertOrUpdateLate = null
            commentAdapter.notifyDataSetChanged()
        }
    }

    override fun onCommentsConnectionProblem() {
        onConnectionProblemBase()
    }

    //VoteView
    override fun onVoteSuccess(commentId: Long) {
        commentManager.loadCommentsByIds(longArrayOf(commentId))
        Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()
    }

    override fun onVoteFail() {
        Toast.makeText(context, R.string.internet_problem, Toast.LENGTH_SHORT).show()
    }

    // begin  DeleteCommentDialogFragment.DialogCallback

    override fun onFailDeleteComment() {
        Toast.makeText(context, R.string.fail_delete_comment, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteConnectionProblem() {
        onConnectionProblemBase()
    }

    override fun onCommentWasDeleted(comment: Comment) {
        handleCommentCountWasUpdated(comment)
    }

    //     end DeleteCommentDialogFragment.DialogCallback

    private fun handleCommentCountWasUpdated(comment: Comment) {
        needInsertOrUpdateLate = comment
        //without animation.
        onRefresh() // it can be dangerous, when 10 or more comments was submit by another users.

        commentCountPoster.updateCommentCount()//say to listeners, that count is updated
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NewCommentActivity.requestCode) {
                data?.let {
                    val newComment = data.getParcelableExtra<Comment>(NewCommentActivity.keyComment)

                    handleCommentCountWasUpdated(newComment)
                }
            }
        }
    }

}