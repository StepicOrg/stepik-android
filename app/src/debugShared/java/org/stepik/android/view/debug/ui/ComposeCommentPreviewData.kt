package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepik.android.model.Step
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.comment.ui.dialog.ComposeCommentDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object ComposeCommentPreviewData {
    fun show(host: Fragment) {
        val thread = DiscussionThread(
            id = "debug-preview",
            thread = DiscussionThread.THREAD_DEFAULT,
            discussionsCount = 0,
            discussionProxy = ""
        )
        ComposeCommentDialogFragment
            .newInstance(thread, Step(id = -1L), parent = null, comment = null, submission = null)
            .showIfNotExists(host.childFragmentManager, ComposeCommentDialogFragment.TAG)
    }
}
