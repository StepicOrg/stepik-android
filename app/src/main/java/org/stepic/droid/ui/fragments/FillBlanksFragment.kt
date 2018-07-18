package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.view_fill_blanks.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.FillBlanksAdapter
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.Reply

class FillBlanksFragment: StepAttemptFragment() {
    private val fillBlanksAdapter = FillBlanksAdapter()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fillBlanksView = layoutInflater.inflate(R.layout.view_fill_blanks, attemptContainer, false)

        with(fillBlanksView.recycler) {
            isNestedScrollingEnabled = false
            adapter = fillBlanksAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        attemptContainer.addView(fillBlanksView)
    }

    override fun showAttempt(attempt: Attempt) {
        val components = attempt.getDataset()?.components ?: return
        fillBlanksAdapter.componentList = components
    }

    override fun generateReply(): Reply {
        val blanks = fillBlanksAdapter.componentList.filter { it.type?.canSubmit() == true }.map { it.defaultValue ?: "" }
        return Reply(blanks = blanks)
    }

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        fillBlanksAdapter.setAllItemsEnabled(!needBlock)
    }

    override fun onRestoreSubmission() {
        val blanksFromReply = submission.reply?.blanks ?: return

        var i = 0 // todo: refactor in order to make this code more convenient
        for (component in fillBlanksAdapter.componentList) {
            if (component.type?.canSubmit() == true && i < blanksFromReply.size) {
                component.defaultValue = blanksFromReply[i]
                i++
            }
        }

        fillBlanksAdapter.notifyDataSetChanged()
    }
}