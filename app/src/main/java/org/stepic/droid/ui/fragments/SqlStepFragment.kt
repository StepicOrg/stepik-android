package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.fragment_step_attempt.*
import kotlinx.android.synthetic.main.view_code_editor.*
import kotlinx.android.synthetic.main.view_code_editor_layout.*
import kotlinx.android.synthetic.main.view_code_toolbar.*
import org.stepic.droid.R
import org.stepik.android.model.attempts.Attempt
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.util.listenKeyboardChanges
import org.stepic.droid.ui.util.stopListenKeyboardChanges
import org.stepik.android.model.Reply

class SqlStepFragment: StepAttemptFragment(), CodeToolbarAdapter.OnSymbolClickListener {
    companion object {
        private const val SQL_LANG = "sql"
        fun newInstance(): SqlStepFragment = SqlStepFragment()
    }

    private var codeToolbarAdapter: CodeToolbarAdapter? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeToolbarAdapter = CodeToolbarAdapter(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewGroup = layoutInflater.inflate(R.layout.view_sql_quiz, attemptContainer, false) as ViewGroup
        attemptContainer.addView(viewGroup)
        codeEditor.lang = SQL_LANG

        codeToolbarView.adapter = codeToolbarAdapter
        codeToolbarAdapter?.onSymbolClickListener = this
        codeToolbarAdapter?.setLanguage(SQL_LANG)
        codeToolbarView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        codeEditor.codeToolbarAdapter = codeToolbarAdapter
    }

    override fun onStart() {
        super.onStart()
        onGlobalLayoutListener = listenKeyboardChanges(
                rootFrameLayoutInStepAttempt,
                onKeyboardShown = {
                    codeToolbarView.visibility = View.VISIBLE
                    codeToolbarSpaceInContainer.visibility = View.VISIBLE
                },
                onKeyboardHidden = {
                    codeToolbarView.visibility = View.GONE
                    codeToolbarSpaceInContainer.visibility = View.GONE
                }
        )
    }

    override fun onStop() {
        super.onStop()
        stopListenKeyboardChanges(rootFrameLayoutInStepAttempt, onGlobalLayoutListener)
        onGlobalLayoutListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codeToolbarAdapter?.onSymbolClickListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        codeToolbarAdapter = null
    }

    override fun onSymbolClick(symbol: String) {
        codeEditor.insertText(symbol)
    }

    override fun showAttempt(attempt: Attempt?) {
        // no-op
    }

    override fun generateReply() = Reply(solveSql = codeEditor.text.toString())


    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        codeEdit.isEnabled = !needBlock
    }

    override fun onRestoreSubmission() {
        codeEditor.setText(submission.reply?.solveSql)
    }
}