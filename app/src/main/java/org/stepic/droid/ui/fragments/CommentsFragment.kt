package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.error_no_connection.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.CommentManager
import org.stepic.droid.core.commentcount.contract.CommentCountPoster
import org.stepik.android.model.user.User
import org.stepic.droid.model.comments.*
import org.stepic.droid.ui.adapters.CommentsAdapter
import org.stepic.droid.ui.util.ContextMenuRecyclerView
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.*
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.view.injection.step.StepDiscussionBus
import java.util.*
import javax.inject.Inject

@SuppressFBWarnings(value = ["RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"], justification = "false positive: commentManager is not null")
class CommentsFragment : FragmentBase(),
        SwipeRefreshLayout.OnRefreshListener {
    companion object {
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


        fun newInstance(discussionId: String, stepId: Long, needInstaOpen: Boolean): Fragment =
                CommentsFragment().also {
                    it.discussionId = discussionId
                    it.stepId = stepId
                    it.needInstaOpen = needInstaOpen
                }
    }

    @Inject
    lateinit var commentManager: CommentManager

    lateinit var commentAdapter: CommentsAdapter

    var needInsertOrUpdateLate: Comment? = null
    val links = ArrayList<String>()

    @Inject
    lateinit var commentCountPoster: CommentCountPoster

    @Inject
    @field:StepDiscussionBus
    lateinit var stepDiscussionSubject: PublishSubject<Long>

    private var discussionId: String by argument()
    private var stepId: Long by argument()
    private var needInstaOpen: Boolean by argument()


    override fun injectComponent() {
        App
                .componentManager()
                .stepComponent(stepId)
                .commentsComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onReleaseComponent() {
        super.onReleaseComponent()
        App
                .componentManager()
                .releaseStepComponent(stepId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentAdapter = CommentsAdapter(commentManager, context)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_comments, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initToolbar()
        initSwipeRefreshLayout()
        initRecyclerView()

        showEmptyProgressOnCenter()
        if (commentManager.isEmpty()) {
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

    private fun initRecyclerView() {
        recyclerViewComments.adapter = commentAdapter
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.isSmoothScrollbarEnabled = false
        recyclerViewComments.layoutManager = linearLayoutManager
        registerForContextMenu(recyclerViewComments)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

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

    override fun onStop() {
        super.onStop()
        cancelSwipeRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        swipeRefreshLayoutComments.setOnRefreshListener(null)
        addNewCommentButton.setOnClickListener(null)
        unregisterForContextMenu(recyclerViewComments)
    }

    override fun onRefresh() {
        commentManager.reset()
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

    //     end DeleteCommentDialogFragment.DialogCallback

    private fun handleCommentCountWasUpdated(comment: Comment) {
        needInsertOrUpdateLate = comment
        //without animation.
        onRefresh() // it can be dangerous, when 10 or more comments was submit by another users.

        commentCountPoster.updateCommentCount()//say to listeners, that count is updated
        stepDiscussionSubject.onNext(stepId)
    }
}