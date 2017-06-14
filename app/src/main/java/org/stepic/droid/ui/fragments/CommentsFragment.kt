package org.stepic.droid.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.appbar_only_toolbar.*
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.internet_fail_clickable.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.Client
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.CommentManager
import org.stepic.droid.core.comments.contract.CommentsListener
import org.stepic.droid.core.presenters.DiscussionPresenter
import org.stepic.droid.core.presenters.contracts.DiscussionView
import org.stepic.droid.events.comments.*
import org.stepic.droid.model.User
import org.stepic.droid.model.comments.*
import org.stepic.droid.ui.adapters.CommentsAdapter
import org.stepic.droid.ui.dialogs.DeleteCommentDialogFragment
import org.stepic.droid.ui.util.ContextMenuRecyclerView
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.StringUtil
import org.stepic.droid.util.getFirstAndLastName
import org.stepic.droid.web.VoteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject


class CommentsFragment : FragmentBase(),
        SwipeRefreshLayout.OnRefreshListener,
        DiscussionView,
        CommentsListener {

    companion object {
        private val discussionIdKey = "dis_id_key"
        private val stepIdKey = "stepId"

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


        fun newInstance(discussionId: String, stepId: Long): Fragment {
            val args = Bundle()
            args.putString(discussionIdKey, discussionId)
            args.putLong(stepIdKey, stepId)
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


    override fun injectComponent() {
        App
                .component()
                .commentsComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentAdapter = CommentsAdapter(commentManager, context)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_comments, container, false)

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discussionId = arguments.getString(discussionIdKey)
        stepId = arguments.getLong(stepIdKey)
        setHasOptionsMenu(true)
        initToolbar()
        initSwipeRefreshLayout()
        initRecyclerView()
        initAddCommentButton()

        commentsClient.subscribe(this)
        discussionPresenter.attachView(this)
        bus.register(this)

        showEmptyProgressOnCenter()
        if (commentManager.isEmpty()) {
            loadDiscussionProxyById()
        } else {
            showEmptyProgressOnCenter(false)
        }
    }

    private fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar as Toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayoutComments.setOnRefreshListener(this)
        swipeRefreshLayoutComments.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon)
    }

    private fun initAddCommentButton() {
        addNewCommentButton.setOnClickListener {
            stepId?.let {
                screenManager.openNewCommentForm(activity, it, null)
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
            if (comment.user != null && comment.user.toLong() != userId && comment.vote != null) {
                //it is not current user and vote is available
                val vote = commentManager.getVoteByVoteId(comment.vote)
                if (vote?.value != null && vote.value == VoteValue.like) {
                    //if we have like -> show suggest for unlike
                    menu?.add(Menu.NONE, unLikeMenuId, Menu.NONE, R.string.unlike_label)
                } else {
                    menu?.add(Menu.NONE, likeMenuId, Menu.NONE, R.string.like_label)
                }
                menu?.add(Menu.NONE, reportMenuId, Menu.NONE, R.string.report_label)
            }
        }

        val commentUser: User? = comment.user?.let { commentManager.getUserById(it) }
        if (commentUser?.first_name?.isNotBlank() ?: false || commentUser?.last_name?.isNotBlank() ?: false) {
            val userNameText: String? = commentUser?.getFirstAndLastName()
            val spannableUserName = SpannableString(userNameText)
            spannableUserName.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.black)), 0, spannableUserName.length, 0)

            val userMenuItem = menu?.add(Menu.NONE, userMenuId, Menu.NONE, spannableUserName)
            userMenuItem?.titleCondensed = userNameText
        }

        if (userId > 0) {
            if (comment.actions?.delete ?: false) {
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

            in linksStartIndexId..linksStartIndexId + firstLinkShift - 1 -> {
                val index = item.itemId
                clickLinkInComment(links[index - linksStartIndexId])
                return true
            }

            else -> return super.onContextItemSelected(item)
        }
    }

    private fun openUserProfile(position: Int) {
        if (position < 0 && position >= commentManager.getSize()) return

        val comment = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        if (comment.user != null) {
            val userId = commentManager.getUserById(comment.user)?.id
            if (userId != null) {
                analytic.reportEvent(Analytic.Profile.CLICK_USER_IN_COMMENT)
                screenManager.openProfile(activity, userId.toLong())
            }
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
            if (!dialog.isAdded) {
                dialog.show(fragmentManager, null)
            }
        }

    }

    private fun replyToComment(position: Int) {
        val comment: Comment? = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        comment?.let {
            screenManager.openNewCommentForm(activity, stepId, it.parent ?: it.id)
        }
    }

    private fun likeComment(position: Int) {
        vote(position, VoteValue.like)
    }

    private fun unlikeComment(position: Int) {
        vote(position, null)
    }

    private fun abuseComment(position: Int) {
        vote(position, VoteValue.dislike)
    }

    private fun vote(position: Int, voteValue: VoteValue?) {
        if (sharedPreferenceHelper.authResponseFromStore == null) {
            Toast.makeText(context, R.string.anonymous_like_mark_comment, Toast.LENGTH_SHORT).show()
            return
        }


        val comment = commentManager.getItemWithNeedUpdatingInfoByPosition(position).comment
        val voteId = comment.vote
        val commentId = comment.id ?: return
        voteId?.let {
            val voteObject = Vote(voteId, voteValue)
            api.makeVote(it, voteValue).enqueue(object : Callback<VoteResponse> {
                override fun onResponse(call: Call<VoteResponse>?, response: Response<VoteResponse>?) {
                    //todo event for update
                    if (response?.isSuccessful ?: false) {
                        bus.post(LikeCommentSuccessEvent(commentId, voteObject))
                    } else {
                        bus.post(LikeCommentFailEvent())
                    }
                }

                override fun onFailure(call: Call<VoteResponse>?, t: Throwable?) {
                    //todo event for fail
                    bus.post(LikeCommentFailEvent())
                }
            })
        }

    }

    @Subscribe
    fun onLikeCommentFail(event: LikeCommentFailEvent) {
        Toast.makeText(context, R.string.internet_problem, Toast.LENGTH_SHORT).show()
    }

    @Subscribe
    fun onLikeCommentSuccess(event: LikeCommentSuccessEvent) {
        commentManager.loadCommentsByIds(longArrayOf(event.commentId))
        Toast.makeText(context, R.string.done, Toast.LENGTH_SHORT).show()
    }

    private fun loadDiscussionProxyById(id: String = discussionId) {
        discussionPresenter.loadDiscussion(id)
    }

    @Subscribe
    fun onInternetConnectionProblem(event: InternetConnectionProblemInCommentsEvent) {
        if (event.discussionProxyId.isNullOrBlank() || event.discussionProxyId == discussionId) {
            cancelSwipeRefresh()
            if (commentManager.isEmpty()) {
                showInternetConnectionProblem()
            } else {
                Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
                commentManager.clearAllLoadings()
                commentAdapter.notifyDataSetChanged()
            }
        }
    }


    @Subscribe
    fun onNeedUpdate(needUpdateEvent: NewCommentWasAddedOrUpdateEvent) {
        if (needUpdateEvent.targetId == stepId) {
            if (needUpdateEvent.newCommentInsertOrUpdate != null) {
                //share for updating:
                needInsertOrUpdateLate = needUpdateEvent.newCommentInsertOrUpdate
            }
            //without animation.
            onRefresh() // it can be dangerous, when 10 or more comments was submit by another users.
        }
    }

    override fun onStop() {
        super.onStop()
        cancelSwipeRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        commentsClient.unsubscribe(this)
        bus.unregister(this)
        discussionPresenter.detachView(this)

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
            ProgressHelper.activate(loadProgressbar)
            showEmptyState(false)
            showInternetConnectionProblem(false)
        } else {
            ProgressHelper.dismiss(loadProgressbar)
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

    @Subscribe
    fun onFailDeleteComment(event: FailDeleteCommentEvent) {
        Toast.makeText(context, R.string.fail_delete_comment, Toast.LENGTH_SHORT).show()
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
            val longArr = listOf(needInsertLocal.id, needInsertLocal.parent).filterNotNull().toLongArray()
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


}