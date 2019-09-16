package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import kotlinx.android.synthetic.main.empty_comments.*
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import kotlinx.android.synthetic.main.error_no_connection.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.CommentManager
import org.stepic.droid.model.comments.*
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.*
import javax.inject.Inject

@SuppressFBWarnings(value = ["RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"], justification = "false positive: commentManager is not null")
class CommentsFragment : FragmentBase() {
    companion object {
        fun newInstance(discussionId: String, stepId: Long, needInstaOpen: Boolean): Fragment =
                CommentsFragment().also {
                    it.discussionId = discussionId
                    it.stepId = stepId
                    it.needInstaOpen = needInstaOpen
                }
    }

    @Inject
    lateinit var commentManager: CommentManager

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_comments, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initToolbar()

        showEmptyProgressOnCenter()
        if (commentManager.isEmpty()) {
        } else {
            showEmptyProgressOnCenter(false)
        }
    }

    private fun initToolbar() {
        initCenteredToolbar(R.string.comments_title, true, getCloseIconDrawableRes())
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        DiscussionOrder.values().forEach {
            if (it.menuId == item?.itemId) {
                sharedPreferenceHelper.discussionOrder = it
                item.isChecked = true

                commentManager.resetAll()

                showEmptyProgressOnCenter()
                commentManager.loadComments()
                //todo reload comments, set to comment manager, and resolve above by id from preferences.
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}