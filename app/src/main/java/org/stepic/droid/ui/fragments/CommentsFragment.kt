package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.CommentManager
import org.stepic.droid.model.comments.*
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.*
import javax.inject.Inject

@SuppressFBWarnings(value = ["RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"], justification = "false positive: commentManager is not null")
class CommentsFragment : FragmentBase() {

    @Inject
    lateinit var commentManager: CommentManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initToolbar()
    }

    private fun initToolbar() {
        initCenteredToolbar(R.string.comments_title, true, getCloseIconDrawableRes())
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        DiscussionOrder.values().forEach {
            if (it.menuId == item?.itemId) {
                sharedPreferenceHelper.discussionOrder = it
                item.isChecked = true

                commentManager.resetAll()

                commentManager.loadComments()
                //todo reload comments, set to comment manager, and resolve above by id from preferences.
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}