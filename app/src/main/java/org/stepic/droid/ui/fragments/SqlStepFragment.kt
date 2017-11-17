package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_code_editor_layout.*
import org.stepic.droid.R
import org.stepic.droid.model.Attempt
import org.stepic.droid.model.Reply

class SqlStepFragment: StepAttemptFragment() {
    companion object {
        private const val SQL_LANG = "sql"
        fun newInstance(): SqlStepFragment = SqlStepFragment()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewGroup = layoutInflater.inflate(R.layout.view_sql_quiz, attemptContainer, false) as ViewGroup
        attemptContainer.addView(viewGroup)
        codeEditor.lang = SQL_LANG
    }

    override fun showAttempt(attempt: Attempt?) {

    }

    override fun generateReply() =
        Reply.Builder().setSolveSql(codeEditor.text.toString()).build()


    override fun blockUIBeforeSubmit(needBlock: Boolean) {

    }

    override fun onRestoreSubmission() {

    }
}