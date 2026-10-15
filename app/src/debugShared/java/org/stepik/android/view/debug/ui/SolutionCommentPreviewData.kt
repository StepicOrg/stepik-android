package org.stepik.android.view.debug.ui

import androidx.fragment.app.Fragment
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Block
import org.stepik.android.model.Reply
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.comment.ui.dialog.SolutionCommentDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

object SolutionCommentPreviewData {
    fun show(host: Fragment) {
        val step = Step(id = -1L, block = Block(name = AppConstants.TYPE_STRING))
        val attempt = Attempt(id = -1L, step = step.id)
        val submission = Submission(
            id = -1L,
            attempt = attempt.id,
            status = Submission.Status.CORRECT,
            _reply = Reply(text = "Sample answer")
        )
        SolutionCommentDialogFragment
            .newInstance(step, attempt, submission)
            .showIfNotExists(host.childFragmentManager, SolutionCommentDialogFragment.TAG)
    }
}
